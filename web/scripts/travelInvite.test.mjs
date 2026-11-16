import assert from "node:assert/strict"
import { execFileSync } from "node:child_process"
import { mkdtempSync, rmSync, writeFileSync } from "node:fs"
import { tmpdir } from "node:os"
import path from "node:path"
import { test } from "node:test"
import { pathToFileURL } from "node:url"

const output = mkdtempSync(path.join(tmpdir(), "oguri-travel-invite-"))
let travelInviteModel
try {
  execFileSync(process.execPath, [
    "node_modules/typescript/bin/tsc",
    "src/features/routing/travelInviteModel.ts",
    "--module", "ES2022", "--target", "ES2020", "--moduleResolution", "Bundler",
    "--skipLibCheck", "--outDir", output,
  ], { stdio: "pipe" })
  writeFileSync(path.join(output, "package.json"), '{"type":"module"}')
  const compiled = await import(pathToFileURL(path.join(output, "travelInviteModel.js")).href)
  travelInviteModel = compiled.travelInviteModel
} finally {
  rmSync(output, { recursive: true, force: true })
}

const token = "A".repeat(43)

test("valid invitations keep the token only in the app URL fragment", () => {
  const model = travelInviteModel(`#${token}`, ["ko-KR"])
  const url = new URL(model.appUrl)
  assert.equal(url.protocol, "oguri:")
  assert.equal(url.hostname, "open")
  assert.equal(url.pathname, "/trip-invite")
  assert.equal(url.search, "")
  assert.equal(url.hash, `#${token}`)
  assert.ok(!model.description.includes(token))
})

test("malformed fragments cannot produce an app-opening URL", () => {
  for (const hash of ["", token, "#short", `#${token}&redirect=evil`, `#${token}/`, `#${token}%20`, `#${token}\n`, `#${token}\r`, `#${"A".repeat(42)}\n`]) {
    assert.equal(travelInviteModel(hash, ["en"]).appUrl, null, hash)
  }
})

test("all four languages provide invitation and invalid-link messages", () => {
  for (const language of ["en-US", "ko-KR", "ja-JP", "zh-Hans-CN"]) {
    const valid = travelInviteModel(`#${token}`, [language])
    const invalid = travelInviteModel("", [language])
    assert.equal(valid.language, language.slice(0, 2))
    assert.ok(valid.openLabel.length > 0)
    assert.ok(valid.installInstructions.length > 0)
    assert.notEqual(valid.description, invalid.description)
  }
})

test("language selection honors supported preferences and falls back to English", () => {
  assert.equal(travelInviteModel("", ["fr-FR", "JA-jp"]).language, "ja")
  assert.equal(travelInviteModel("", ["ko_KR"]).language, "ko")
  assert.equal(travelInviteModel("", ["fr-FR"]).language, "en")
  assert.equal(travelInviteModel("", []).language, "en")
})

test("untrusted invalid fragments are never echoed into page copy", () => {
  const malicious = '#<img src=x onerror="alert(1)">'
  const model = travelInviteModel(malicious, ["ko"])
  assert.equal(model.appUrl, null)
  assert.ok(!JSON.stringify(model).includes("onerror"))
})
