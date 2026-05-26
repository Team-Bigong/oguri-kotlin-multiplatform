import React from "react"
import { OfficialHomePage } from "./features/official/OfficialHomePage"
import { AdminApp } from "./features/admin/AdminApp"
import { resolveIsAdminPath } from "./features/routing/pathResolver"

export const App = (): React.JSX.Element => {
  const [isAdminPath, setIsAdminPath] = React.useState<boolean>(resolveIsAdminPath())

  React.useEffect(() => {
    const handleLocationChanged = (): void => {
      setIsAdminPath(resolveIsAdminPath())
    }
    window.addEventListener("hashchange", handleLocationChanged)
    window.addEventListener("popstate", handleLocationChanged)
    return () => {
      window.removeEventListener("hashchange", handleLocationChanged)
      window.removeEventListener("popstate", handleLocationChanged)
    }
  }, [])

  if (isAdminPath) {
    return <AdminApp />
  }
  return <OfficialHomePage />
}
