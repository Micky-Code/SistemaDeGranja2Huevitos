package sistema.inventario.postura;
import java.time.*;

public abstract class EventoDiario {
	
protected LocalDate fechaRegistro;
String responsable;

public abstract void aplicar(Lote lote);
}
