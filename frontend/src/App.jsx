import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import RutaPrivada from "./RutaPrivada";
import Login from "./pages/Login";
import ClienteAgendarCita from "./pages/ClienteAgendarCita";
import BarberoCalendario from "./pages/BarberoCalendario";
import DuenoDashboard from "./pages/DuenoDashboard";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route
            path="/cliente"
            element={
              <RutaPrivada rol="CLIENTE">
                <ClienteAgendarCita />
              </RutaPrivada>
            }
          />
          <Route
            path="/barbero"
            element={
              <RutaPrivada rol="BARBERO">
                <BarberoCalendario />
              </RutaPrivada>
            }
          />
          <Route
            path="/dueno"
            element={
              <RutaPrivada rol="DUENO">
                <DuenoDashboard />
              </RutaPrivada>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
