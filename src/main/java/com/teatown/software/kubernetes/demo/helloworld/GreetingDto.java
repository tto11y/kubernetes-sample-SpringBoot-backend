package com.teatown.software.kubernetes.demo.helloworld;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class GreetingDto {

    private String text;

}
