import { travelInviteModel } from "./travelInviteModel"

function renderInvitation() {
  const languages = navigator.languages.length ? navigator.languages : [navigator.language]
  const model = travelInviteModel(window.location.hash, languages)
  document.documentElement.lang = model.language
  document.title = `${model.title} · Oguri`
  const fields = {
    "invite-title": model.title,
    "invite-description": model.description,
    "open-app": model.openLabel,
    "install-instructions": model.installInstructions,
  }
  Object.entries(fields).forEach(([id, value]) => {
    const element = document.getElementById(id)
    if (element) element.textContent = value
  })
  const openApp = document.querySelector<HTMLAnchorElement>("#open-app")
  if (openApp) {
    if (model.appUrl) openApp.href = model.appUrl
    else openApp.removeAttribute("href")
    openApp.hidden = model.appUrl === null
  }
  const installation = document.getElementById("installation")
  if (installation) installation.hidden = model.appUrl === null
}

renderInvitation()
window.addEventListener("hashchange", renderInvitation)
