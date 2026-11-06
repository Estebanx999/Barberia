import { Route, Routes } from 'react-router-dom';
import { MarcoApp } from './componentes/MarcoApp';
import { InicioSesion } from './paginas/InicioSesion';
import { NoEncontrada, SinAcceso } from './paginas/Mensajes';
import { Panel } from './paginas/Panel';
import { Registro } from './paginas/Registro';
import { RedireccionInicial, RutaProtegida, SoloInvitados } from './rutas/RutaProtegida';

export function App() {
  return (
    <Routes>
      <Route element={<SoloInvitados />}>
        <Route path="/ingresar" element={<InicioSesion />} />
        <Route path="/registro" element={<Registro />} />
      </Route>

      <Route element={<MarcoApp />}>
        <Route element={<RutaProtegida roles={['CLIENTE']} />}>
          <Route path="/cliente" element={<Panel />} />
        </Route>
        <Route element={<RutaProtegida roles={['BARBERO']} />}>
          <Route path="/barbero" element={<Panel />} />
        </Route>
        <Route element={<RutaProtegida roles={['DUENO']} />}>
          <Route path="/dueno" element={<Panel />} />
        </Route>
      </Route>

      <Route path="/" element={<RedireccionInicial />} />
      <Route path="/sin-acceso" element={<SinAcceso />} />
      <Route path="*" element={<NoEncontrada />} />
    </Routes>
  );
}
