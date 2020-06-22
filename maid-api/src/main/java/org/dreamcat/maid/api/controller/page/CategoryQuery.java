package org.dreamcat.maid.api.controller.page;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.maid.api.core.LastPageQuery;

import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/6/17
 */
@Getter
@Setter
public class CategoryQuery extends LastPageQuery {
    @NotNull
    @JsonDeserialize(using = Category.CategoryDeserializer.class)
    private Category category;

}
