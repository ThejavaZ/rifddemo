namespace RFID.Application;

// Errores de validación esperados: el adaptador HTTP los responde como 400 { error }.
public sealed class ValidationException(string message) : Exception(message);
