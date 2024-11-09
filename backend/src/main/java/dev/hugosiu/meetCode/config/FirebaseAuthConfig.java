package dev.hugosiu.meetCode.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

import static dev.hugosiu.meetCode.constant.CodeExecuteConstant.SERVICE_ACCOUNT;

@Configuration
public class FirebaseAuthConfig {
  @Bean
  FirebaseAuth firebaseAuth() throws IOException {
    System.out.println("========== FirebaseAuthConfig invoked! ==========");

    FileInputStream serviceAccount =
            new FileInputStream(SERVICE_ACCOUNT);

    FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

    var firebaseApp = FirebaseApp.initializeApp(options);

    return FirebaseAuth.getInstance(firebaseApp);
  }
}
