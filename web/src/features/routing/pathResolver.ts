export const resolveIsAdminPath = (): boolean => {
  return window.location.pathname.startsWith("/admin") || window.location.hash.startsWith("#/admin")
}
