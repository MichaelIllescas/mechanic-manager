# Product Vision

**Versión:** 1.0  
**Estado:** MVP simplificado para validación  
**Producto:** Mechanic Manager

---

## 1. Visión

Construir un micro SaaS simple para que un taller mecánico pequeño o mediano pueda administrar su operación diaria desde un único lugar, reemplazando registros en papel, planillas y datos dispersos.

La primera versión se diseña para **validar la utilidad del producto con usuarios reales**, no para resolver desde el inicio todos los escenarios posibles de una plataforma SaaS madura.

El modelo inicial será deliberadamente simple:

```text
Usuario operativo -> un taller -> operación del taller
```

Cada usuario operativo administra su propio taller. La plataforma puede tener muchos usuarios, pero en el MVP no existe la necesidad de que un usuario gestione varios talleres ni de que un taller tenga varios usuarios.

---

## 2. Problema

En muchos talleres mecánicos la información operativa se encuentra distribuida entre agendas manuales, mensajes, hojas de cálculo, presupuestos en papel y registros informales.

Esto dificulta:

- conocer qué vehículos están agendados para cada día;
- recuperar rápidamente la información de un cliente;
- identificar correctamente un vehículo;
- conservar número de patente y, cuando estén disponibles, chasis y motor;
- generar y consultar presupuestos;
- conocer el estado actual de un trabajo;
- recuperar reparaciones anteriores;
- mantener un historial técnico confiable.

---

## 3. Usuarios objetivo

### 3.1 Administrador del sistema (`SYSTEM_ADMIN`)

Representa al equipo responsable de Mechanic Manager.

Responsabilidades del MVP:

- crear, consultar, habilitar y deshabilitar usuarios de la plataforma;
- administrar el acceso de esos usuarios;
- consultar el estado básico de las cuentas;
- ejecutar funciones administrativas globales estrictamente necesarias.

El Administrador del sistema **no crea ni administra talleres**, no configura la operación de un taller y no participa del trabajo diario de sus usuarios.

### 3.2 Usuario del taller (`USER`)

Es el único rol operativo del MVP.

Responsabilidades:

- crear y configurar su propio taller;
- administrar clientes;
- administrar vehículos;
- administrar agenda y turnos;
- generar presupuestos;
- definir los catálogos de servicios e insumos del taller;
- registrar los trabajos realizados;
- consultar historial de vehículos.

En el MVP cada `USER` puede gestionar un único taller y tiene acceso completo a la operación de ese taller.

No se diferencian roles internos como propietario, recepción, encargado o mecánico. Esos escenarios se evaluarán después de validar el producto.

---

## 4. Propuesta de valor

El producto debe proporcionar:

- una única fuente de información para el taller;
- organización de agenda y turnos;
- identificación completa de clientes y vehículos;
- generación y seguimiento de presupuestos;
- trazabilidad de los trabajos realizados;
- historial técnico por vehículo;
- separación segura de la información de cada usuario/taller;
- una experiencia suficientemente simple para poder ser probada y validada rápidamente.

---

## 5. Flujo de valor principal

```text
Configurar Taller
       ↓
Agenda
       ↓
Cliente / Vehículo
       ↓
Presupuesto
       ↓
Orden de trabajo
       ↓
Diagnóstico / Servicios realizados
       ↓
Finalización / Entrega
       ↓
Historial
```

---

## 6. Criterio de éxito del MVP

El MVP será considerado funcional cuando un usuario pueda completar el siguiente flujo sin depender de registros externos:

1. El Administrador del sistema crea o habilita una cuenta de usuario.
2. El Usuario inicia sesión.
3. Crea o completa los datos de su taller.
4. Consulta la agenda.
5. Registra o selecciona un cliente.
6. Registra o selecciona un vehículo.
7. Agenda un turno.
8. Genera un presupuesto.
9. Completa el turno cuando el vehículo ingresa, y se genera el trabajo realizado con los ítems del presupuesto aprobado.
10. Ajusta el trabajo realizado: ítems, kilometraje y observaciones.
11. Consulta posteriormente el historial técnico del vehículo o del cliente y descarga el comprobante.

Además, un usuario nunca debe poder consultar o modificar información perteneciente al taller de otro usuario.

---

## 7. Principio de evolución

No se diseñará por anticipado para escenarios que todavía no fueron validados.

Las siguientes capacidades sólo se incorporarán si la experiencia con clientes demuestra que son necesarias:

- varios talleres por usuario;
- varios usuarios dentro de un taller;
- invitaciones de colaboradores;
- roles y permisos internos;
- administración centralizada de talleres;
- estructuras organizacionales más complejas.
