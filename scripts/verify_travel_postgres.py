#!/usr/bin/env python3
"""Run server tests against an isolated, temporary PostgreSQL instance."""

import hashlib
import io
import os
import platform
import shutil
import signal
import socket
import subprocess
import sys
import tarfile
import tempfile
import zipfile
from pathlib import Path

POSTGRES_VERSION = "17.11.0"
MAVEN_ROOT = "https://repo.maven.apache.org/maven2/io/zonky/test/postgres"
REPOSITORY = Path(__file__).resolve().parents[1]


def run(arguments, **options):
    return subprocess.run([str(value) for value in arguments], check=True, **options)


def postgres_bin(workspace):
    configured = os.environ.get("TRAVEL_POSTGRES_BIN")
    installed = shutil.which("postgres")
    if configured or installed:
        directory = Path(configured) if configured else Path(installed).parent
        for name in ("postgres", "initdb", "pg_ctl"):
            if not (directory / name).is_file():
                raise RuntimeError(f"Missing PostgreSQL executable: {directory / name}")
        return directory
    if platform.system() != "Darwin" or platform.machine() != "arm64":
        raise RuntimeError("Set TRAVEL_POSTGRES_BIN to a PostgreSQL bin directory on this platform.")
    if sys.version_info < (3, 12):
        raise RuntimeError("Portable PostgreSQL extraction requires Python 3.12 or later.")
    artifact = "embedded-postgres-binaries-darwin-arm64v8"
    url = f"{MAVEN_ROOT}/{artifact}/{POSTGRES_VERSION}/{artifact}-{POSTGRES_VERSION}.jar"
    bundle = workspace / "postgres.jar"
    checksum = workspace / "postgres.sha256"
    for source, target in ((url, bundle), (url + ".sha256", checksum)):
        run(["curl", "--fail", "--silent", "--show-error", "--max-time", "120", source, "-o", target])
    if hashlib.sha256(bundle.read_bytes()).hexdigest() != checksum.read_text().strip().split()[0]:
        raise RuntimeError("PostgreSQL bundle checksum mismatch")
    with zipfile.ZipFile(bundle) as archive:
        payloads = [name for name in archive.namelist() if name.endswith(".txz")]
        if len(payloads) != 1:
            raise RuntimeError("Unexpected PostgreSQL bundle layout")
        with tarfile.open(fileobj=io.BytesIO(archive.read(payloads[0])), mode="r:xz") as binaries:
            binaries.extractall(workspace / "runtime", filter="data")
    return next((workspace / "runtime").rglob("bin/postgres")).parent


def main():
    workspace = Path(tempfile.mkdtemp(prefix="oguri-travel-pg-", dir="/tmp" if Path("/tmp").is_dir() else None))
    data = workspace / "data"
    pg = None
    try:
        pg = postgres_bin(workspace)
        run([pg / "postgres", "--version"])
        run([pg / "initdb", "-D", data, "-U", "postgres", "-A", "trust", "--no-locale", "-E", "UTF8"], stdout=subprocess.DEVNULL)
        with socket.socket() as reservation:
            reservation.bind(("127.0.0.1", 0))
            port = reservation.getsockname()[1]
        # Only disposable fixtures are stored here; this server listens on loopback only.
        run([pg / "pg_ctl", "-D", data, "-l", workspace / "server.log", "-o",
             f"-h 127.0.0.1 -p {port} -k {workspace}", "-w", "start"])
        environment = dict(os.environ)
        environment.update({
            "TRAVEL_TEST_DATABASE_URL": f"jdbc:postgresql://127.0.0.1:{port}/postgres",
            "TRAVEL_TEST_DATABASE_USER": "postgres",
            "TRAVEL_TEST_DATABASE_PASSWORD": "postgres",
        })
        run([REPOSITORY / "gradlew", ":server:test"], cwd=REPOSITORY, env=environment)
    finally:
        stopped = True
        if pg and (data / "postmaster.pid").exists():
            stopped = subprocess.run([str(pg / "pg_ctl"), "-D", str(data), "-m", "fast", "-w", "stop"], check=False).returncode == 0
        if stopped:
            shutil.rmtree(workspace)
        else:
            print(f"Could not stop the test database; preserved its files at {workspace}", file=sys.stderr)


def cancelled(_signal, _frame):
    raise KeyboardInterrupt


if __name__ == "__main__":
    signal.signal(signal.SIGTERM, cancelled)
    try:
        main()
    except KeyboardInterrupt:
        print("PostgreSQL verification cancelled", file=sys.stderr)
        sys.exit(130)
    except (RuntimeError, subprocess.CalledProcessError) as failure:
        print(f"PostgreSQL verification failed: {failure}", file=sys.stderr)
        sys.exit(1)
