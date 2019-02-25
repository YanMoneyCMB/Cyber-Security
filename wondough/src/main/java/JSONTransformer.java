package wondough;

import spark.*;
import com.google.gson.Gson;

/**
* A ResponseTransformer for JSON data.
* @author The Intern
*/
public class JSONTransformer implements ResponseTransformer {
    /**
    * The Gson object used to transform the response body
    * to a JSON object.
    */
    private Gson gson = new Gson();

    /**
    * Renders the response model to a JSON object.
    */
    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
}
