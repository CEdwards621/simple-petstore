package org.testinfected.petstore.controllers;

import org.testinfected.petstore.Controller;
import org.testinfected.petstore.procurement.ProcurementRequestHandler;
import org.testinfected.petstore.product.DuplicateProductException;

public class CreateProduct implements Controller {

    private final ProcurementRequestHandler requestHandler;

    public CreateProduct(ProcurementRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void process(Request request, Response response) throws Exception {
        try {
            requestHandler.addProductToCatalog(
                    request.getParameter("number"),
                    request.getParameter("name"),
                    request.getParameter("description"),
                    request.getParameter("photo"));
            response.renderHead(HttpCodes.CREATED);
        } catch (DuplicateProductException e) {
            response.renderHead(HttpCodes.CONFLICT);
        }
    }
}