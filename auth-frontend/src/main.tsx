import { createRoot } from "react-dom/client";
import "./index.css";
import { BrowserRouter, Route, Routes } from "react-router";
import RootLayout from "./pages/RootLayout.tsx";
import App from "./App.tsx";
import Login from "./pages/Login.tsx";

createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<RootLayout />}>
        <Route index element={<App />} />
        <Route path="/login" element={<Login/>}/>
      </Route>
    </Routes>
  </BrowserRouter>,
);
