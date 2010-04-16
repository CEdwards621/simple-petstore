package test.com.pyxis.petstore.view;

import com.pyxis.petstore.domain.billing.CreditCardType;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;
import test.support.com.pyxis.petstore.views.ModelBuilder;
import test.support.com.pyxis.petstore.views.VelocityRendering;

import java.util.ArrayList;
import java.util.List;

import static com.pyxis.matchers.dom.DomMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static test.support.com.pyxis.petstore.builders.CartBuilder.aCart;
import static test.support.com.pyxis.petstore.builders.ItemBuilder.anItem;
import static test.support.com.pyxis.petstore.views.ModelBuilder.aModel;
import static test.support.com.pyxis.petstore.views.PathFor.purchasesPath;
import static test.support.com.pyxis.petstore.views.VelocityRendering.render;

public class NewPurchaseViewTest {

    String NEW_PURCHASE_VIEW_NAME = "purchases/new";
    Element newPurchaseView;
    ModelBuilder model;

    @Before public void
    renderView() {
        model = aModel().
                with(aCart().containing(anItem().priced("100.00"))).
                and("cardTypes", CreditCardType.values());
        newPurchaseView = renderNewPurchaseView().using(model).asDom();
    }

    @Test public void
    displaysOrderSummary() {
        assertThat(newPurchaseView, hasUniqueSelector("#cart-grand-total", withText("100.00")));
    }

    @Test public void
    displaysPurchaseForm() {
        assertThat(newPurchaseView, hasUniqueSelector("form#checkout",
                withAttribute("action", purchasesPath()),
                withAttribute("method", "post"),
                withBillingInformation(),
                withPaymentDetails(),
                withSubmitOrderButton()));
    }

    @Test public void
    fillsCardTypeSelectionList() {
        assertThat(newPurchaseView, hasSelector("#card-type option", withCreditCardOptions()));
    }

    private Matcher<Element> withBillingInformation() {
        return hasUniqueSelector("#billing-address", withInputFields(
                withName("billingAddress.firstName"),
                withName("billingAddress.lastName"),
                withName("billingAddress.emailAddress")));
    }

    private Matcher<Element> withPaymentDetails() {
        return hasUniqueSelector("#payment",
                withSelectionLists(
                        withName("cardType")),
                withInputFields(
                        withName("cardNumber"),
                        withName("cardExpiryDate"))
        );
    }

    private Matcher<Element> withSelectionLists(final Matcher<Element>... dropDownMatchers) {
        return hasSelector("select", dropDownMatchers);
    }

    private Matcher<Element> withSubmitOrderButton() {
        return hasUniqueSelector("button#submit");
    }

    private Matcher<Iterable<Element>> withCreditCardOptions() {
        List<Matcher<? super Element>> matchers = new ArrayList<Matcher<? super Element>>();
        for (CreditCardType type : CreditCardType.values()) {
            matchers.add(withOption(type.name(), type.getCommonName()));
        }
        return containsInAnyOrder(matchers);
    }

    private Matcher<Element> withOption(String value, String text) {
        return allOf(withAttribute("value", value), withText(text));
    }

    @Ignore @Test public void indicatesWhenCartIsEmpty() {}


    @Ignore @Test public void returnsToCartPageToReviewOrder() {}

    private Matcher<Element> withInputFields(final Matcher<Element>... fieldMatchers) {
        return hasSelector("input[type='text']", fieldMatchers);
    }

    private VelocityRendering renderNewPurchaseView() {
        return render(NEW_PURCHASE_VIEW_NAME);
    }
}
