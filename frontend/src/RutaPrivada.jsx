import { Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";

export default function RutaPrivada({ children, rol }) {
  const { usuario } = useAuth();
  if (!usuario) return <Navigate to="/" replace />;
  if (rol && usuario.rol !== rol) return <Navigate to="/" replace />;
  return children;
}
