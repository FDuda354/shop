package pl.dudios.shopmvn.order.model.dto;

import lombok.Getter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
public class OrderDto {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String street;
    @NotBlank
    private String zipCode;
    @NotBlank
    private String city;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String phone;
    @NotBlank
    private Long basketId;
    @NotBlank
    private Long shipmentId;
    @NotBlank
    private Long paymentId;
}
