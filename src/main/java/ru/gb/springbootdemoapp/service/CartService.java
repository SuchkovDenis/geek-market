package ru.gb.springbootdemoapp.service;

import java.security.Principal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import ru.gb.springbootdemoapp.converter.ProductMapper;
import ru.gb.springbootdemoapp.dto.Cart;
import ru.gb.springbootdemoapp.repository.CartRepository;

@Service
public class CartService {

  private ProductService productService;
  private ProductMapper productMapper;
  private CartRepository cartRepository;

  public CartService(ProductService productService, ProductMapper productMapper, CartRepository cartRepository) {
    this.productService = productService;
    this.productMapper = productMapper;
    this.cartRepository = cartRepository;
  }

  public Cart getCartForCurrentUser() {
    Principal principal = SecurityContextHolder.getContext().getAuthentication();
    String sessinid = RequestContextHolder.currentRequestAttributes().getSessionId();
    Cart cart = getCart(principal, sessinid);
    return cart;
  }

  public void removeCartForCurrentUser() {
    cartRepository.delete(getCartForCurrentUser());
  }

  private Cart getCart(Principal principal, String sessinid) {
    // TODO что делать когда аноним набрал что-то в корзину и залогинился ???
    if (principal instanceof AnonymousAuthenticationToken) {
      return cartRepository.findById(sessinid).orElse(new Cart(sessinid));
    }
    return cartRepository.findById(principal.getName()).orElse(new Cart(principal.getName()));
  }

  public Cart addProductById(Long id) {
    Cart cart = getCartForCurrentUser();
    productService.findById(id).ifPresent(product -> cart.addItem(productMapper.productToCartItem(product)));
    cartRepository.save(cart);
    return cart;
  }

  public Cart removeProductById(Long id) {
    Cart cart = getCartForCurrentUser();
    cart.removeItem(id);
    cartRepository.save(cart);
    return cart;
  }
}
