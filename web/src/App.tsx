import React from "react"
import { OfficialHomePage } from "./features/official/OfficialHomePage"
import { AdminApp } from "./features/admin/AdminApp"

const isAdminPath = window.location.pathname.startsWith("/admin")

export const App = (): React.JSX.Element => {
  if (isAdminPath) {
    return <AdminApp />
  }
  return <OfficialHomePage />
}
