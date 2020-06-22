package org.dreamcat.maid.api.controller.file;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dreamcat.common.web.util.JacksonUtil;

/**
 * Create by tuke on 2020/6/10
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IdNameView {
    @JsonSerialize(using = JacksonUtil.LongSerializer.class)
    private Long id;
    private String name;
}
