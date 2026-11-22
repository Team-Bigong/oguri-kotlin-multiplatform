#!/usr/bin/env python3
"""Import GeoNames populated places (CC BY 4.0) into the migrated Oguri PostgreSQL database."""
import argparse
import csv
import io
import re
import subprocess
import tempfile
import zipfile
from collections import Counter, defaultdict
from pathlib import Path
from unicodedata import normalize
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError


def zip_rows(directory, filename):
    with zipfile.ZipFile(directory / (filename + ".zip")) as archive:
        with io.TextIOWrapper(archive.open(filename + ".txt"), encoding="utf-8") as rows:
            for row in rows:
                yield row.rstrip("\r\n").split("\t")


def text_rows(path):
    with path.open(encoding="utf-8") as rows:
        for row in rows:
            if not row.startswith("#"):
                yield row.rstrip("\r\n").split("\t")


def valid_zone(name):
    try:
        ZoneInfo(name)
    except (ValueError, ZoneInfoNotFoundError):
        return False
    return bool(name)


def prepare(source, staging, compact):
    countries = {row[0]: row[4] for row in text_rows(source / "countryInfo.txt") if len(row) > 4}
    regions = {row[0]: row[1] for row in text_rows(source / "admin1CodesASCII.txt")}
    zones = {}
    places = {}
    excluded = Counter()
    for row in zip_rows(source, "allCountries"):
        if row[6] != "P":
            continue
        if row[7] in {"PPLH", "PPLQ", "PPLW"}:
            excluded["historic, abandoned or destroyed"] += 1
            continue
        if row[17] not in zones:
            zones[row[17]] = valid_zone(row[17])
        if not re.fullmatch("[A-Z]{2}", row[8]) or not zones[row[17]] or not row[1].strip():
            excluded["missing country, timezone or name"] += 1
            continue
        identifier, name, ascii_name = row[:3]
        country = row[8]
        country_label = countries.get(country, country)
        region = regions.get(country + "." + row[10])
        area = " · ".join(dict.fromkeys(part for part in (country_label, region) if part))
        places[identifier] = (name, ascii_name, country, area, row[17], row[4], row[5], row[14] or "0")

    aliases = defaultdict(set)
    korean_names = {}
    for row in zip_rows(source, "alternateNamesV2"):
        identifier, language, name = row[1:4]
        language = language.split("-", 1)[0]
        if identifier not in places or language not in {"ko", "en"} or row[7] == "1" or row[9]:
            continue
        name = normalize("NFC", name.strip())
        if not name:
            continue
        aliases[identifier].add(name)
        if language == "ko" and re.search("[가-힣]", name):
            candidate = (row[4] != "1", row[5] != "1", row[6] == "1", len(name), name)
            korean_names[identifier] = min(candidate, korean_names.get(identifier, candidate))

    if compact:
        included = {row[0] for row in zip_rows(source, "cities500")} | korean_names.keys()
        places = {identifier: place for identifier, place in places.items() if identifier in included}

    names = {}
    labels = Counter()
    for identifier, (name, _, country, area, *_) in places.items():
        name = korean_names[identifier][-1] if identifier in korean_names else normalize("NFC", name.strip())
        names[identifier] = name[:65].strip(), area[:22].strip() if len(area) > 22 and len(name) > 60 else area.strip()
        labels[names[identifier]] += 1

    alias_count = 0
    with (staging / "cities.csv").open("w", encoding="utf-8", newline="") as city_rows, \
         (staging / "aliases.csv").open("w", encoding="utf-8", newline="") as alias_rows:
        city_writer = csv.writer(city_rows)
        alias_writer = csv.writer(alias_rows)
        for identifier, (original, ascii_name, country, _, zone, latitude, longitude, population) in places.items():
            name, area = names[identifier]
            suffix = " (" + identifier + ")"
            if labels[(name, area)] > 1 or len(name + " · " + area) > 100:
                area = area[:97 - len(name) - len(suffix)].strip() + suffix
            city_writer.writerow((identifier, name, country, area, zone, latitude, longitude, population))
            variants = aliases.get(identifier, set()) | {normalize("NFC", value.strip()) for value in (original, ascii_name)}
            for variant in sorted(variants - {"", name}):
                alias_writer.writerow((identifier, variant))
                alias_count += 1
    print(f"GeoNames CC BY 4.0: {len(places):,} places, {len(korean_names):,} Korean labels, {alias_count:,} aliases", flush=True)
    print(f"Excluded: {dict(excluded)}", flush=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="GeoNames dump download directory")
    parser.add_argument("--psql", default="psql", help="PostgreSQL client; connection uses standard PG environment variables")
    parser.add_argument("--compact", action="store_true", help="import cities500 plus places with Korean labels")
    args = parser.parse_args()
    with tempfile.TemporaryDirectory(prefix="oguri-city-import-") as staging:
        staging = Path(staging)
        prepare(args.source, staging, args.compact)
        sql = r"""BEGIN;
LOCK TABLE cities, city_aliases IN SHARE ROW EXCLUSIVE MODE;
CREATE TEMP TABLE incoming_cities (LIKE cities) ON COMMIT DROP;
CREATE TEMP TABLE incoming_aliases (LIKE city_aliases) ON COMMIT DROP;
\copy incoming_cities FROM 'cities.csv' CSV
\copy incoming_aliases FROM 'aliases.csv' CSV
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM cities c JOIN incoming_cities i USING (id)
        WHERE (c.latitude, c.longitude, c.country_code) IS DISTINCT FROM (i.latitude, i.longitude, i.country_code)
    ) THEN
        RAISE EXCEPTION 'Existing city IDs conflict with the GeoNames import';
    END IF;
END $$;
INSERT INTO cities SELECT * FROM incoming_cities ON CONFLICT (id) DO NOTHING;
INSERT INTO city_aliases SELECT * FROM incoming_aliases ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('cities', 'id'), (SELECT max(id) FROM cities));
COMMIT;
ANALYZE cities;
ANALYZE city_aliases;
"""
        subprocess.run([args.psql, "-X", "-w", "-v", "ON_ERROR_STOP=1"], cwd=staging, input=sql, text=True, check=True)


if __name__ == "__main__":
    main()
