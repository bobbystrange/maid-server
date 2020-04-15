package org.dreamcat.maid.api.controller.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Create by tuke on 2020/2/26
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ImageCodeQuery {
    @NotEmpty
    @Size(min = 4, max = 255)
    private String proof;
    @NotNull
    @Min(16)
    @Max(512)
    private Integer width;
    @NotNull
    @Min(16)
    @Max(512)
    private Integer height;
}
