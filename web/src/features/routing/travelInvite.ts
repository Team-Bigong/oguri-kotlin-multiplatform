const token = window.location.hash.slice(1)
const openApp = document.querySelector<HTMLAnchorElement>("#open-app")

if (openApp && /^[A-Za-z0-9_-]{43}$/.test(token)) {
  openApp.href = `oguri://open/trip-invite#${token}`
  openApp.hidden = false
}
