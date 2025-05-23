package com.syu.itzy_mayo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment implements AuthStateObserver{
    
    private FirebaseAuth firebaseAuth;
    private UserSessionManager sessionManager;
    private LinearLayout notLoggedInView;
    private LinearLayout loggedInView;
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button githubLoginButton;
    private Button registerButton;
    private Button logoutButton;
    private TextView displayName;
    private Toast toast;
    private boolean isLogoutHandled;

    private void showToast(String message){
        if (toast != null) {
            toast.cancel();
        }
        if(getContext() != null) {
            toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_settings, container, false);

        // UserSessionManager 초기화
        sessionManager = ItzyMayoApplication.getInstance().getSessionManager();
        sessionManager.addObserver(this);
        firebaseAuth = ItzyMayoApplication.getInstance().getFirebaseAuth();
        initializeViews(rootView);
        updateLoginUI();
        setupSettingsClickListeners(rootView);
        super.onStart();
        return rootView;

    }

    private void initializeViews(View rootView) {
        notLoggedInView = rootView.findViewById(R.id.not_logged_in_view);
        loggedInView = rootView.findViewById(R.id.logged_in_view);
        emailInput = rootView.findViewById(R.id.email_input);
        passwordInput = rootView.findViewById(R.id.password_input);
        loginButton = rootView.findViewById(R.id.login_button);
        githubLoginButton = rootView.findViewById(R.id.github_login_button);
        registerButton = rootView.findViewById(R.id.register_button);
        logoutButton = rootView.findViewById(R.id.logout_button);
        displayName = rootView.findViewById(R.id.display_name);
        
        // 버튼 클릭 리스너 설정
        loginButton.setOnClickListener(v -> loginUser());
        githubLoginButton.setOnClickListener(v -> loginUserWithGithub());
        registerButton.setOnClickListener(v -> registerUser());
        logoutButton.setOnClickListener(v -> logoutUser());
    }
    
    private void updateLoginUI() {
        if (!isAdded()) return;

        if (sessionManager.isLoggedIn()) {
            // 로그인 상태 UI
            notLoggedInView.setVisibility(View.GONE);
            loggedInView.setVisibility(View.VISIBLE);
            displayName.setText(sessionManager.getDisplayName());
        } else {
            // 로그아웃 상태 UI
            notLoggedInView.setVisibility(View.VISIBLE);
            loggedInView.setVisibility(View.GONE);
        }
    }
    
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            showToast("이메일과 비밀번호를 입력해주세요.");
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener(requireActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("로그인 실패: " + e.getMessage());
                    }
                }
        );;
    }
    private void loginUserWithGithub() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");
        List<String> scopes =
                new ArrayList<String>() {
                    {
                        add("user:email");
                    }
                };
        provider.setScopes(scopes);
        firebaseAuth.startActivityForSignInWithProvider(requireActivity(), provider.build())
                .addOnFailureListener(requireActivity(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast("로그인 실패: " + e.getMessage());
                        }
                    }
                );

    }
    
    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            showToast("이메일과 비밀번호를 입력해주세요.");
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        // 세션 생성
                        sessionManager.createLoginSession(user);
                        showToast("회원가입 성공: " + user.getEmail());
                        updateLoginUI();
                        clearInputFields();
                    } else {
                        showToast("회원가입 실패: " + task.getException());
                    }
                }
            });
    }

    private void logoutUser() {
        sessionManager.logoutUser();
        showToast("로그아웃 되었습니다.");
    }
    
    private void clearInputFields() {
        emailInput.setText("");
        passwordInput.setText("");
    }

    private void setupSettingsClickListeners(View rootView) {
        TextView notificationSettings = rootView.findViewById(R.id.notification_settings);
        TextView accountSettings = rootView.findViewById(R.id.account_settings);
        TextView aboutApp = rootView.findViewById(R.id.about_app);

        notificationSettings.setOnClickListener(v -> showToast("알림 설정"));

        accountSettings.setOnClickListener(v -> {
            // 계정 설정은 로그인 상태에서만 접근 가능
            if (sessionManager.isLoggedIn()) {
                showToast("계정 설정");
            } else {
                showToast("로그인이 필요합니다.");
            }
        });

        aboutApp.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("About Us")
                    .setMessage("모달 메시지 내용을 여기에 작성합니다.")
                    .setPositiveButton("확인", (dialog, which) -> {
                        // 확인 버튼 클릭 시 동작
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                        // 취소 버튼 클릭 시 동작
                    });

            AlertDialog dialog = builder.create();
            dialog.show();});
    }
    @Override
    public void onStart() {
        super.onStart();
        updateLoginUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLoginUI();
    }

    @Override
    public void onAuthStateChanged(FirebaseUser user) {
        updateLoginUI();
    }
}