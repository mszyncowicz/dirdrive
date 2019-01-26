package org.fytyny.dirdrive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Random;
import java.util.UUID;

@Entity
@Data
public class Session {
    public static final int scale = 20;

    @Id
    @JsonIgnore
    private UUID id;

    @Column(length = scale)
    private String token;

    @ManyToOne(targetEntity = ApiKey.class, optional = false)
    @JsonIgnore
    private ApiKey apiKey;

    public Session() {
    }

    public static String generateRandom(int scale) {
        String space = "ABCDEFGHIJKLMNOPQRSTUWXVYZabcdefghijklmnopqrstuwvxyz12345667890";
        Random random = new Random();
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < scale; i++) {
            int getInt = random.nextInt(space.length());
            result.append(space.charAt(getInt));

        }
        return result.toString();
    }

}
