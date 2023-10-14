package br.com.itdev.todolist.login;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.itdev.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFilter extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getServletPath().startsWith("/tasks/")) {
            var authEncoded = request.getHeader("Authorization").substring("Basic".length()).trim();
            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            String authString = new String(authDecoded);
            String[] credentials = authString.split(":");

            String username = credentials[0];
            String password = credentials[1];

            var userAuth = this.userRepository.findByUsername(username);

            if (userAuth == null) {
                response.sendError(401, "Usuário sem autorização");
            } else {
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), userAuth.getPassword());

                if (passwordVerify.verified) {
                    request.setAttribute("idUser", userAuth.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }

}
