package com.mycompany.app;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Base64;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class JWTEngine {

    public static String getUserID(JSONObject decodedJSON, String secret64Encoded, boolean debug) {
    
        if (debug == false) {

            String user_id = null;

            try {

                String token = decodedJSON.getJSONObject("auth").getString("token");

                //System.out.println(decodedJSON.toString());

                byte[] secret64Decoded = Base64.getDecoder().decode(secret64Encoded);

                Algorithm algorithm = Algorithm.HMAC256(secret64Decoded);
                JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
                DecodedJWT jwt = verifier.verify(token);

                user_id = jwt.getClaim("user_id").asString();
                if (user_id == null) {
                    System.out.println("User is not authenticated");
                }

            } catch (Exception e){
                System.out.println("Exception at verifying JWT");
                e.printStackTrace();
            }

            return user_id;

        } else {

            return "53062560";
        }

    } 

}

