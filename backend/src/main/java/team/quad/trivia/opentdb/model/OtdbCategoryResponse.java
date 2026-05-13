package team.quad.trivia.opentdb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import team.quad.trivia.model.Question;

import java.util.List;

public record OtdbCategoryResponse(
        @JsonProperty("trivia_categories") List<OtdbCategory> triviaCategories
){

    public record OtdbCategory(
            int id,
            String name
    ) {
        public Question.Category toDomain() {
            return new Question.Category(id, name);
        }
    }

    public List<Question.Category> toDomain() {
        return this.triviaCategories.stream()
                .map(OtdbCategory::toDomain)
                .toList();
    }

}