import { Route, Routes } from 'react-router-dom';
import { AvisoServidor } from './componentes/AvisoServidor';
import { MarcoApp } from './componentes/MarcoApp';
import { MiAgenda } from './paginas/barbero/MiAgenda';
import { MisComisiones } from './paginas/barbero/MisComisiones';
import { RegistrarCorteSinCita } from './paginas/barbero/RegistrarCorteSinCita';
import { Gastos } from './paginas/dueno/Gastos';
import { OperacionDia } from './paginas/dueno/OperacionDia';
import { Reportes } from './paginas/dueno/Reportes';
import { AgendarCita } from './paginas/cliente/AgendarCita';
import { MisCitas } from './paginas/cliente/MisCitas';
import { InicioSesion } from './paginas/InicioSesion';
import { NoEncontrada, SinAcceso } from './paginas/Mensajes';
import { Registro } from './paginas/Registro';
import { RedireccionInicial, RutaProtegida, SoloInvitados } from './rutas/RutaProtegida';

export function App() {
  return (
    <>
      <AvisoServidor />
      <Routes>
        <Route element={<SoloInvitados />}>
          <Route path="/ingresar" element={<InicioSesion />} />
          <Route path="/registro" element={<Registro />} />
        </Route>

        <Route element={<MarcoApp />}>
          <Route element={<RutaProtegida roles={['CLIENTE']} />}>
            <Route path="/cliente" element={<MisCitas />} />
            <Route path="/cliente/agendar" element={<AgendarCita />} />
          </Route>
          <Route element={<RutaProtegida roles={['BARBERO']} />}>
            <Route path="/barbero" element={<MiAgenda />} />
            <Route path="/barbero/sin-cita" element={<RegistrarCorteSinCita />} />
            <Route path="/barbero/comisiones" element={<MisComisiones />} />
          </Route>
          <Route element={<RutaProtegida roles={['DUENO']} />}>
            <Route path="/dueno" element={<OperacionDia />} />
            <Route path="/dueno/mi-agenda" element={<MiAgenda />} />
            <Route path="/dueno/sin-cita" element={<RegistrarCorteSinCita />} />
            <Route path="/dueno/gastos" element={<Gastos />} />
            <Route path="/dueno/reportes" element={<Reportes />} />
          </Route>
        </Route>

        <Route path="/" element={<RedireccionInicial />} />
        <Route path="/sin-acceso" element={<SinAcceso />} />
        <Route path="*" element={<NoEncontrada />} />
      </Routes>
    </>
  );
}
