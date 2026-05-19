import Navbar from '@/components/Navbar'
import { Outlet } from 'react-router'
import { Toaster } from "react-hot-toast";

function RootLayout() {
  return (
    <div>
      <Toaster/>
      <Navbar/>
      <Outlet/>
    </div>
  )
}

export default RootLayout
