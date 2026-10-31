package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.ContourWaveCanvas
import com.example.ui.components.FloatingOrb
import com.example.ui.theme.*

@Composable
fun AuthScreen(
  onLogin: (email: String, pass: String) -> String?,
  onSignUp: (name: String, email: String, pass: String, role: UserRole) -> String?,
  onContinueAsGuest: () -> Unit,
  onQuickLoginPreset: (email: String, pass: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var isSignUp by remember { mutableStateOf(false) }

  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf(UserRole.PRINCIPAL_COLLECTOR) }
  var isPasswordVisible by remember { mutableStateOf(false) }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.testTag("auth_screen"),
    containerColor = AethelBackground
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Background subtle contour wave
      ContourWaveCanvas(
        modifier = Modifier.fillMaxSize(),
        lineColor = AethelNeonPurple.copy(alpha = 0.25f),
        strokeCount = 20
      )

      // Floating Orbs
      FloatingOrb(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = 10.dp, y = 30.dp),
        size = 54.dp,
        primaryColor = AethelNeonPink,
        secondaryColor = AethelNeonPurple
      )

      FloatingOrb(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .offset(x = (-10).dp, y = (-40).dp),
        size = 44.dp,
        primaryColor = AethelNeonCyan,
        secondaryColor = AethelPrimary
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
      ) {
        // App Identity Header
        item {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.linearGradient(listOf(AethelSurfaceElevated, Color(0xFF1E1430)))
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AccountBalance,
              contentDescription = null,
              tint = AethelNeonCyan,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "AETHEL VAULT",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
          )

          Text(
            text = "Fine Art Provenance & Private Collection",
            color = AethelTextSecondary,
            fontSize = 13.sp
          )

          Spacer(modifier = Modifier.height(28.dp))
        }

        // Toggle Switch: Sign In vs Sign Up
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = AethelSurfaceElevated,
            border = BorderStroke(1.dp, AethelBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(4.dp)) {
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    isSignUp = false
                    errorMessage = null
                  }
                  .testTag("tab_sign_in"),
                color = if (!isSignUp) AethelPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = "Sign In",
                  color = if (!isSignUp) Color.White else AethelTextSecondary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier
                    .padding(vertical = 10.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                )
              }

              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    isSignUp = true
                    errorMessage = null
                  }
                  .testTag("tab_sign_up"),
                color = if (isSignUp) AethelPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = "Create Account",
                  color = if (isSignUp) Color.White else AethelTextSecondary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier
                    .padding(vertical = 10.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))
        }

        // Form Card
        item {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AethelSurface.copy(alpha = 0.95f)),
            border = BorderStroke(1.dp, AethelBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              if (errorMessage != null) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFF33151D),
                  border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.5f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = errorMessage ?: "",
                      color = Color(0xFFFF6B6B),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }

              // Name field on Sign Up
              if (isSignUp) {
                OutlinedTextField(
                  value = name,
                  onValueChange = { name = it },
                  label = { Text("Curator / Collector Name") },
                  placeholder = { Text("e.g. Moyo Shiro") },
                  leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AethelTextSecondary) },
                  singleLine = true,
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                  colors = authTextFieldColors()
                )

                // Role selector
                Column {
                  Text(
                    text = "SELECT YOUR VAULT ROLE",
                    color = AethelTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    UserRole.values().forEach { role ->
                      val isSel = selectedRole == role
                      Surface(
                        modifier = Modifier
                          .weight(1f)
                          .clip(RoundedCornerShape(10.dp))
                          .clickable { selectedRole = role },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) AethelPrimary.copy(alpha = 0.35f) else AethelSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSel) AethelPrimary else AethelBorder)
                      ) {
                        Text(
                          text = role.name.replace("_", " ").take(9),
                          color = if (isSel) Color.White else AethelTextMuted,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier
                            .padding(vertical = 8.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                      }
                    }
                  }
                }
              }

              // Email
              OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("collector@aethelvault.art") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AethelTextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                colors = authTextFieldColors()
              )

              // Password
              OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AethelTextSecondary) },
                trailingIcon = {
                  IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                      imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                      contentDescription = "Toggle Password",
                      tint = AethelTextSecondary
                    )
                  }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                colors = authTextFieldColors()
              )

              // Confirm Password on Sign Up
              if (isSignUp) {
                OutlinedTextField(
                  value = confirmPassword,
                  onValueChange = { confirmPassword = it },
                  label = { Text("Confirm Password") },
                  leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = AethelTextSecondary) },
                  visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                  singleLine = true,
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_input"),
                  colors = authTextFieldColors()
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              // Submit Button
              Button(
                onClick = {
                  errorMessage = null
                  if (isSignUp) {
                    if (password != confirmPassword) {
                      errorMessage = "Passwords do not match."
                      return@Button
                    }
                    val err = onSignUp(name, email, password, selectedRole)
                    if (err != null) errorMessage = err
                  } else {
                    val err = onLogin(email, password)
                    if (err != null) errorMessage = err
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("auth_submit_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AethelPrimary)
              ) {
                Text(
                  text = if (isSignUp) "Create Free Account" else "Authenticate & Enter Vault",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))
        }

        // Quick Demo Accounts & Guest Option
        item {
          Text(
            text = "QUICK TEST CREDENTIALS",
            color = AethelTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { onQuickLoginPreset("moyo@aethelvault.art", "collector2026") },
              modifier = Modifier.weight(1f).testTag("quick_free_user_btn"),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, AethelBorder)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Free User", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("(10 art, 5 music limits)", color = AethelTextMuted, fontSize = 9.sp)
              }
            }

            OutlinedButton(
              onClick = { onQuickLoginPreset("vip@aethelvault.art", "collector2026") },
              modifier = Modifier.weight(1f).testTag("quick_vip_user_btn"),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.7f))
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VIP Subscriber", color = AethelAmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("(Unlimited & PDF unlocked)", color = AethelTextMuted, fontSize = 9.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          TextButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.testTag("continue_as_guest_btn")
          ) {
            Text("Explore Vault as Guest", color = AethelNeonCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedContainerColor = AethelSurfaceElevated,
  unfocusedContainerColor = AethelBackground,
  focusedBorderColor = AethelPrimary,
  unfocusedBorderColor = AethelBorder,
  focusedTextColor = Color.White,
  unfocusedTextColor = Color.White
)
