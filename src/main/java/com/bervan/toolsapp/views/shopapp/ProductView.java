package com.bervan.toolsapp.views.shopapp;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.service.ProductBasedOnDateAttributesService;
import com.bervan.shstat.service.ProductService;
import com.bervan.shstat.tokens.ProductSimilarOffersService;
import com.bervan.shstat.view.AbstractProductView;
import com.bervan.shstat.view.ProductViewService;
import com.bervan.toolsapp.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Deprecated
@Route(value = AbstractProductView.ROUTE_NAME, layout = MainLayout.class)
@RolesAllowed("USER")
public class ProductView extends AbstractProductView {

    public ProductView(ProductViewService productViewService,
                       ProductRepository productRepository,
                       ProductService productService,
                       ProductBasedOnDateAttributesService productBasedOnDateAttributesService,
                       ProductSimilarOffersService productSimilarOffersService, BervanViewConfig bervanViewConfi) {
        super(productViewService, productService,
                productSimilarOffersService, productRepository, productBasedOnDateAttributesService, bervanViewConfi);
    }
}
