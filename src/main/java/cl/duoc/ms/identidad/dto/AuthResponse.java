package cl.duoc.ms.identidad.dto;

public record AuthResponse (
    String token,
    String tipoToken
){

}
