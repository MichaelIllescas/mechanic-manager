import { describe, expect, it } from 'vitest';

import {
  MONTH_GRID_DAYS,
  addDays,
  daysFor,
  minutesFromMidnight,
  parseLocalDateTime,
  rangeFor,
  shift,
  stackOverlapping,
  startOfMonthGrid,
  startOfWeek,
  toIsoDate,
  toIsoDateTime,
} from './calendar.utils';

describe('calendar.utils', () => {
  describe('toIsoDate', () => {
    it('usa la fecha local y no la UTC', () => {
      // A las 00:30 en UTC-3 toISOString() daría el día siguiente.
      expect(toIsoDate(new Date(2026, 8, 10, 0, 30))).toBe('2026-09-10');
      // A las 23:30 daría el día anterior en zonas positivas.
      expect(toIsoDate(new Date(2026, 8, 10, 23, 30))).toBe('2026-09-10');
    });

    it('completa mes y día con cero', () => {
      expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
    });
  });

  describe('toIsoDateTime y parseLocalDateTime', () => {
    it('van y vuelven sin corrimiento de zona', () => {
      const original = new Date(2026, 8, 10, 10, 30);
      const parsed = parseLocalDateTime(toIsoDateTime(original));

      expect(parsed.getTime()).toBe(original.getTime());
    });

    it('interpreta el formato del backend como hora local', () => {
      const parsed = parseLocalDateTime('2026-09-10T08:05:00');

      expect(parsed.getFullYear()).toBe(2026);
      expect(parsed.getMonth()).toBe(8);
      expect(parsed.getDate()).toBe(10);
      expect(parsed.getHours()).toBe(8);
      expect(parsed.getMinutes()).toBe(5);
    });
  });

  describe('startOfWeek', () => {
    it('devuelve el lunes de la semana', () => {
      // 2026-09-10 es jueves.
      expect(toIsoDate(startOfWeek(new Date(2026, 8, 10)))).toBe('2026-09-07');
    });

    it('trata al domingo como último día de la semana', () => {
      // 2026-09-13 es domingo: su lunes es el 7, no el 14.
      expect(toIsoDate(startOfWeek(new Date(2026, 8, 13)))).toBe('2026-09-07');
    });

    it('deja el lunes donde está', () => {
      expect(toIsoDate(startOfWeek(new Date(2026, 8, 7)))).toBe('2026-09-07');
    });
  });

  describe('startOfMonthGrid', () => {
    it('arranca en el lunes de la semana del día 1', () => {
      // 2026-09-01 es martes: la grilla arranca el lunes 31 de agosto.
      expect(toIsoDate(startOfMonthGrid(new Date(2026, 8, 20)))).toBe('2026-08-31');
    });

    it('no agrega días cuando el mes empieza lunes', () => {
      // 2026-06-01 es lunes.
      expect(toIsoDate(startOfMonthGrid(new Date(2026, 5, 15)))).toBe('2026-06-01');
    });
  });

  describe('rangeFor', () => {
    it('la vista diaria pide un rango de un día', () => {
      expect(rangeFor('day', new Date(2026, 8, 10))).toEqual({
        from: '2026-09-10',
        to: '2026-09-10',
      });
    });

    it('la vista semanal pide de lunes a domingo', () => {
      expect(rangeFor('week', new Date(2026, 8, 10))).toEqual({
        from: '2026-09-07',
        to: '2026-09-13',
      });
    });

    it('la vista mensual pide la grilla completa de seis semanas', () => {
      expect(rangeFor('month', new Date(2026, 8, 10))).toEqual({
        from: '2026-08-31',
        to: '2026-10-11',
      });
    });

    it('ningún rango supera el tope de 92 días del backend', () => {
      const range = rangeFor('month', new Date(2026, 8, 10));
      const days =
        (new Date(range.to).getTime() - new Date(range.from).getTime()) / 86400000 + 1;

      expect(days).toBeLessThanOrEqual(92);
    });
  });

  describe('daysFor', () => {
    it('devuelve un día, siete o cuarenta y dos según la vista', () => {
      const anchor = new Date(2026, 8, 10);

      expect(daysFor('day', anchor)).toHaveLength(1);
      expect(daysFor('week', anchor)).toHaveLength(7);
      expect(daysFor('month', anchor)).toHaveLength(MONTH_GRID_DAYS);
    });

    it('la grilla mensual cubre el mes entero más el relleno', () => {
      const days = daysFor('month', new Date(2026, 8, 10));

      expect(toIsoDate(days[0])).toBe('2026-08-31');
      expect(toIsoDate(days[days.length - 1])).toBe('2026-10-11');
    });
  });

  describe('shift', () => {
    it('mueve un día, una semana o un mes según la vista', () => {
      const anchor = new Date(2026, 8, 10);

      expect(toIsoDate(shift('day', anchor, 1))).toBe('2026-09-11');
      expect(toIsoDate(shift('week', anchor, 1))).toBe('2026-09-17');
      expect(toIsoDate(shift('month', anchor, 1))).toBe('2026-10-01');
    });

    it('retrocede sin salirse del mes corto', () => {
      // Desde el 31 de marzo, un mes atrás no puede ser el 31 de febrero.
      expect(toIsoDate(shift('month', new Date(2026, 2, 31), -1))).toBe('2026-02-01');
    });
  });

  describe('minutesFromMidnight', () => {
    it('convierte la hora a minutos para posicionar el turno', () => {
      expect(minutesFromMidnight(new Date(2026, 8, 10, 0, 0))).toBe(0);
      expect(minutesFromMidnight(new Date(2026, 8, 10, 10, 30))).toBe(630);
      expect(minutesFromMidnight(new Date(2026, 8, 10, 23, 59))).toBe(1439);
    });
  });

  describe('addDays', () => {
    it('cruza el fin de mes', () => {
      expect(toIsoDate(addDays(new Date(2026, 8, 30), 1))).toBe('2026-10-01');
    });
  });

  describe('stackOverlapping', () => {
    const block = (top: number, height = 56) => ({ top, height });

    it('deja intacto un turno que no se pisa con ninguno', () => {
      const [only] = stackOverlapping([block(0)]);

      expect(only.top).toBe(0);
      expect(only.height).toBe(56);
    });

    it('parte al medio la franja de dos turnos en el mismo horario', () => {
      const [first, second] = stackOverlapping([block(504), block(504)]);

      expect(first.top).toBe(504);
      expect(first.height).toBe(28);
      expect(second.top).toBe(532);
      expect(second.height).toBe(28);
    });

    it('reparte en tres el alto de tres turnos simultáneos', () => {
      const positioned = stackOverlapping([block(0), block(0), block(0)]);

      expect(positioned.map((item) => item.top)).toEqual([0, 56 / 3, (56 / 3) * 2]);
      expect(positioned.every((item) => item.height === 56 / 3)).toBe(true);
    });

    it('no achica turnos que no se pisan', () => {
      const positioned = stackOverlapping([block(0), block(56), block(112)]);

      expect(positioned.map((item) => item.top)).toEqual([0, 56, 112]);
      expect(positioned.every((item) => item.height === 56)).toBe(true);
    });

    it('deja fuera de la tanda al turno que empieza después', () => {
      // Dos a las 9 y uno a las 10: el tercero no se pisa con los primeros.
      const positioned = stackOverlapping([block(504), block(504), block(560)]);

      expect(positioned[2].top).toBe(560);
      expect(positioned[2].height).toBe(56);
    });

    it('ordena los turnos por horario', () => {
      const positioned = stackOverlapping([block(560), block(0), block(280)]);

      expect(positioned.map((item) => item.top)).toEqual([0, 280, 560]);
    });

    it('trata como una tanda a los turnos encadenados', () => {
      // 9:00 y 9:30 se pisan, y 9:30 con 10:00: los tres son una sola tanda,
      // así que se reparten el alto que va de 9:00 a 11:00.
      const positioned = stackOverlapping([block(504), block(532), block(560)]);

      expect(positioned.every((item) => item.height === 112 / 3)).toBe(true);
      expect(positioned[0].top).toBe(504);
      expect(positioned[2].top).toBe(504 + (112 / 3) * 2);
    });
  });
});
