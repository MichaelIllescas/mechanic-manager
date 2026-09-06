import { CalendarView } from './appointment.models';

/** Rango que se le pide al backend, con ambas fechas inclusivas. */
export interface DateRange {
  from: string;
  to: string;
}

const MS_PER_DAY = 24 * 60 * 60 * 1000;

/**
 * Formatea una fecha local como yyyy-MM-dd.
 *
 * No usa toISOString(): eso convierte a UTC y en zonas negativas devuelve el
 * día anterior para cualquier hora de la mañana, lo que haría que la agenda
 * pidiera un día equivocado.
 */
export function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');

  return `${year}-${month}-${day}`;
}

/** Formatea una fecha y hora locales como yyyy-MM-ddTHH:mm:ss, sin zona. */
export function toIsoDateTime(date: Date): string {
  const hours = `${date.getHours()}`.padStart(2, '0');
  const minutes = `${date.getMinutes()}`.padStart(2, '0');

  return `${toIsoDate(date)}T${hours}:${minutes}:00`;
}

/** Parsea el yyyy-MM-ddTHH:mm:ss del backend como hora local. */
export function parseLocalDateTime(value: string): Date {
  const [datePart, timePart = '00:00:00'] = value.split('T');
  const [year, month, day] = datePart.split('-').map(Number);
  const [hours, minutes] = timePart.split(':').map(Number);

  return new Date(year, month - 1, day, hours, minutes);
}

export function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

export function addDays(date: Date, days: number): Date {
  const result = startOfDay(date);
  result.setDate(result.getDate() + days);

  return result;
}

export function addMonths(date: Date, months: number): Date {
  const result = startOfDay(date);
  result.setDate(1);
  result.setMonth(result.getMonth() + months);

  return result;
}

export function isSameDay(a: Date, b: Date): boolean {
  return toIsoDate(a) === toIsoDate(b);
}

/** Lunes de la semana que contiene la fecha: el taller piensa la semana así. */
export function startOfWeek(date: Date): Date {
  const day = startOfDay(date);
  const offset = (day.getDay() + 6) % 7;

  return addDays(day, -offset);
}

/**
 * Primer día de la grilla mensual: el lunes de la semana en la que cae el 1.
 * La grilla arranca antes del día 1 para no dejar celdas huérfanas.
 */
export function startOfMonthGrid(date: Date): Date {
  return startOfWeek(new Date(date.getFullYear(), date.getMonth(), 1));
}

/**
 * La grilla mensual siempre tiene 42 celdas (6 semanas).
 *
 * Un mes puede necesitar 4, 5 o 6 filas según en qué día caiga el 1. Fijar 6
 * evita que la altura del calendario salte al cambiar de mes.
 */
export const MONTH_GRID_DAYS = 42;

/** Rango que hay que pedirle al backend para pintar una vista. */
export function rangeFor(view: CalendarView, anchor: Date): DateRange {
  switch (view) {
    case 'day':
      return { from: toIsoDate(anchor), to: toIsoDate(anchor) };

    case 'week': {
      const start = startOfWeek(anchor);
      return { from: toIsoDate(start), to: toIsoDate(addDays(start, 6)) };
    }

    case 'month': {
      const start = startOfMonthGrid(anchor);
      return {
        from: toIsoDate(start),
        to: toIsoDate(addDays(start, MONTH_GRID_DAYS - 1)),
      };
    }
  }
}

/** Días que muestra cada vista, en orden. */
export function daysFor(view: CalendarView, anchor: Date): Date[] {
  switch (view) {
    case 'day':
      return [startOfDay(anchor)];

    case 'week': {
      const start = startOfWeek(anchor);
      return Array.from({ length: 7 }, (_, index) => addDays(start, index));
    }

    case 'month': {
      const start = startOfMonthGrid(anchor);
      return Array.from({ length: MONTH_GRID_DAYS }, (_, index) => addDays(start, index));
    }
  }
}

/** Mueve el ancla una unidad de la vista hacia adelante o atrás. */
export function shift(view: CalendarView, anchor: Date, direction: 1 | -1): Date {
  switch (view) {
    case 'day':
      return addDays(anchor, direction);
    case 'week':
      return addDays(anchor, direction * 7);
    case 'month':
      return addMonths(anchor, direction);
  }
}

/** Minutos desde la medianoche, para posicionar el turno en la grilla horaria. */
export function minutesFromMidnight(date: Date): number {
  return date.getHours() * 60 + date.getMinutes();
}

/** Bloque posicionable en la grilla horaria. */
export interface TimeBlock {
  top: number;
  height: number;
}

/**
 * Apila uno debajo del otro los bloques que se pisan.
 *
 * El taller puede atender varios vehículos a la vez, así que dos turnos a la
 * misma hora son normales: sin repartirlos, uno tapa al otro y sólo se ve el
 * último.
 *
 * Repartirlos en columnas es lo que hace un calendario común, pero acá el
 * bloque queda tan angosto que no se lee ni la patente. Con el reparto en alto
 * cada turno conserva el ancho del día y pierde altura, que es lo que sobra:
 * una franja de una hora entra tres veces antes de quedar ilegible.
 *
 * Agrupa los bloques en tandas que se solapan entre sí y reparte el alto de la
 * tanda en partes iguales, respetando el orden por horario. Un turno aislado
 * queda intacto.
 *
 * Modifica los bloques recibidos y los devuelve ordenados por horario.
 */
export function stackOverlapping<T extends TimeBlock>(blocks: T[]): T[] {
  blocks.sort((a, b) => a.top - b.top);

  let cluster: T[] = [];
  let clusterTop = 0;
  let clusterEnd = -Infinity;

  const closeCluster = () => {
    const height = (clusterEnd - clusterTop) / cluster.length;

    cluster.forEach((block, index) => {
      block.top = clusterTop + index * height;
      block.height = height;
    });

    cluster = [];
    clusterEnd = -Infinity;
  };

  for (const block of blocks) {
    // Un bloque que empieza después del final de todo lo anterior abre tanda.
    if (cluster.length > 0 && block.top >= clusterEnd) {
      closeCluster();
    }

    if (cluster.length === 0) {
      clusterTop = block.top;
    }

    clusterEnd = Math.max(clusterEnd, block.top + block.height);
    cluster.push(block);
  }

  if (cluster.length > 0) {
    closeCluster();
  }

  return blocks;
}

export function daysBetween(from: Date, to: Date): number {
  return Math.round((startOfDay(to).getTime() - startOfDay(from).getTime()) / MS_PER_DAY);
}
