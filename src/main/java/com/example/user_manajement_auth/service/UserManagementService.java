package com.example.user_manajement_auth.service;

import com.example.user_manajement_auth.dto.ReqRes;
import com.example.user_manajement_auth.entity.OurUsers;
import com.example.user_manajement_auth.repository.UsersRepo;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UserManagementService {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ReqRes register(ReqRes registrationRequest){
        ReqRes resp = new ReqRes();

        try {

            OurUsers ourUsers = new OurUsers();

            ourUsers.setEmail(registrationRequest.getEmail());
            ourUsers.setCity(registrationRequest.getCity());
            ourUsers.setName(registrationRequest.getName());
            ourUsers.setRole(registrationRequest.getRole());
            ourUsers.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            OurUsers ourUsersResult = usersRepo.save(ourUsers);

            if(ourUsersResult.getId() > 0){
                resp.setOurUsers(ourUsersResult);
                resp.setMessage("User Saved Successfully");
                resp.setStatusCode(200);
            }

        } catch (Exception e){
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
        }

        return resp;

    }

    public ReqRes login(ReqRes loginRequest){
        ReqRes response = new ReqRes();

        try {

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");

        } catch (Exception e){

            response.setStatusCode(500);
            response.setMessage(e.getMessage());

        }
        return response;
    }

    public ReqRes refreshToken(ReqRes refreshTokenRequest){
        ReqRes response = new ReqRes();

        try {

            String ourEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            OurUsers users = usersRepo.findByEmail(ourEmail).orElseThrow();

            if(jwtUtils.isTokenValid(refreshTokenRequest.getToken(), users)){
                var jwt = jwtUtils.generateToken(users);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenRequest.getToken());
                response.setExpirationTime("24Hrs");
                response.setMessage("Successfully Refresh Token");
            }
            response.setStatusCode(200);
            return response;

        } catch (Exception e){

            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public ReqRes getAllUsers(){
        ReqRes reqRes = new ReqRes();

        try {

            List<OurUsers> result = usersRepo.findAll();
            if(!result.isEmpty()){
                reqRes.setOurUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
            return reqRes;

        } catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured: " + e.getMessage());
            return reqRes;
        }

    }

    public ReqRes getUserById(Integer id){
        ReqRes reqRes = new ReqRes();

        try {

            OurUsers userById = usersRepo.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
            reqRes.setOurUsers(userById);
            reqRes.setStatusCode(200);
            reqRes.setMessage("Successfully");

        } catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }

        return reqRes;

    }

    public ReqRes deleteUser(Integer userId){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> usersOptional = usersRepo.findById(userId);
            if(usersOptional.isPresent()){
                usersRepo.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes updateUser(Integer userId, OurUsers updatedUser){
        ReqRes reqRes = new ReqRes();

        try {
            Optional<OurUsers> usersOptional = usersRepo.findById(userId);
            if(usersOptional.isPresent()){
                OurUsers existingUser = usersOptional.get();
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setName(updatedUser.getName());
                existingUser.setCity(updatedUser.getCity());
                existingUser.setRole(updatedUser.getRole());

                if(updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()){
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }

                OurUsers savedUser = usersRepo.save(existingUser);
                reqRes.setOurUsers(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User Update Successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured: " + e.getMessage());
        }
        return reqRes;

    }

    public ReqRes getMyInfo(String email){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> usersOptional = usersRepo.findByEmail(email);
            if(usersOptional.isPresent()){
                reqRes.setOurUsers(usersOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User Not Found for update");
            }
        } catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }

}
