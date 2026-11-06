package com.barberia.reporte;

/**
 * Patrón Template Method: esqueleto común de los reportes financieros por
 * periodo (RN-03). generar() fija el orden de los pasos; cada reporte solo
 * define de dónde salen sus datos y cómo se calcula su resultado.
 *
 * <p>Las subclases reciben sus repositorios por constructor (DIP). No llevan
 * {@code @Transactional}: generar() es final y un proxy no podría interceptarlo.
 *
 * @param <D> datos que el reporte obtiene del periodo
 * @param <R> resultado del reporte
 */
public abstract class ReportePeriodo<D, R> {

    /** Método plantilla. */
    public final R generar(Periodo periodo) {
        validar(periodo);
        D datos = obtenerDatos(periodo);
        return calcular(periodo, datos);
    }

    private void validar(Periodo periodo) {
        if (periodo == null) {
            throw new IllegalArgumentException("El periodo es obligatorio");
        }
    }

    protected abstract D obtenerDatos(Periodo periodo);

    protected abstract R calcular(Periodo periodo, D datos);
}
