package grupo6.umbook.model;

public enum GroupState {
    GRUPO_CREADO,  // Estado inicial, aunque en la práctica pasará a CON_MIEMBROS al añadir al creador.
    CON_MIEMBROS,  // Estado normal con uno o más miembros.
    SIN_MIEMBROS,  // Estado intermedio cuando el último miembro se va.
    ELIMINADO      // Estado final, para borrado lógico.
}