import assert from "node:assert/strict"
import { readFile } from "node:fs/promises"

const destination = process.argv[2]
const origin = destination ? new URL(destination) : null
if (origin && (origin.protocol !== "https:" || origin.href !== `${origin.origin}/`)) {
  throw new Error("Use an HTTPS origin without a path, query, or fragment.")
}

const fingerprint = (process.env.ANDROID_APP_LINKS_SHA256?.trim() || process.env.TRAVEL_ANDROID_SIGNING_SHA256?.trim()).toUpperCase()
const appleAppId = (process.env.IOS_UNIVERSAL_LINKS_APP_ID?.trim() || process.env.TRAVEL_IOS_APP_ID?.trim())
assert.match(fingerprint ?? "", /^([A-F0-9]{2}:){31}[A-F0-9]{2}$/, "Set ANDROID_APP_LINKS_SHA256")
assert.match(appleAppId ?? "", /^[A-Z0-9]{10}\.[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+$/, "Set IOS_UNIVERSAL_LINKS_APP_ID")

async function read(relativePath, contentType) {
  if (!origin) {
    const file = relativePath.endsWith("/") ? `${relativePath}index.html` : relativePath
    return readFile(new URL(`../dist${file}`, import.meta.url), "utf8")
  }

  const response = await fetch(new URL(relativePath, origin), {
    redirect: "manual",
    signal: AbortSignal.timeout(10_000),
  })
  assert.equal(response.status, 200, `${relativePath} must respond directly with HTTP 200`)
  assert.equal(response.headers.get("content-type")?.split(";")[0].trim(), contentType, `${relativePath} content type`)
  return response.text()
}

const [page, androidJson, appleJson] = await Promise.all([
  read("/trip-invite/", "text/html"),
  read("/.well-known/assetlinks.json", "application/json"),
  read("/.well-known/apple-app-site-association", "application/json"),
])

assert.match(page, /id="open-app"/, "The web fallback must serve the invitation page")
assert.match(page, /play\.google\.com\/store\/apps\/details/, "The invitation page must include Google Play")
assert.match(page, /apps\.apple\.com\//, "The invitation page must include the App Store")
if (origin) {
  const legacyPath = "/trip-invite"
  const canonicalUrl = new URL("/trip-invite/", origin)
  const response = await fetch(new URL(legacyPath, origin), {
    redirect: "manual",
    signal: AbortSignal.timeout(10_000),
  })
  if ([301, 302, 307, 308].includes(response.status)) {
    assert.equal(new URL(response.headers.get("location") ?? "", origin).href, canonicalUrl.href, "Legacy links must redirect to the same invitation page")
  } else {
    assert.equal(response.status, 200, "Legacy invitation URL must remain reachable")
    assert.match(await response.text(), /id="open-app"/, "Legacy invitation URL must serve the invitation page")
  }
}
const android = JSON.parse(androidJson)
assert.ok(android.some(({ relation, target }) =>
  relation?.includes("delegate_permission/common.handle_all_urls") &&
  target?.namespace === "android_app" &&
  target.package_name === "com.bigong.oguri" &&
  target.sha256_cert_fingerprints?.includes(fingerprint)
), "Android association does not match the distribution signing certificate")

const apple = JSON.parse(appleJson)
for (const path of ["/trip-invite", "/trip-invite/"]) {
  assert.ok(apple.applinks?.details?.some(({ appIDs, components }) =>
    appIDs?.includes(appleAppId) && components?.some((component) => component["/"] === path && component.exclude !== true)
  ), `iOS association does not match the application ID and ${path}`)
}

console.log("Travel invitation page and both domain associations verified.")
