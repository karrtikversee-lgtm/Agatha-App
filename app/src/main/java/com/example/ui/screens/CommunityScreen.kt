package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityPost
import com.example.ui.components.FloatingOrb
import com.example.ui.theme.*

@Composable
fun CommunityScreen(
  posts: List<CommunityPost>,
  onToggleLike: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var newPostContent by remember { mutableStateOf("") }
  var postList by remember { mutableStateOf(posts) }

  Scaffold(
    modifier = modifier.testTag("community_screen"),
    containerColor = AethelBackground
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "GLOBAL ARTIST & COLLECTOR NETWORK",
              color = AethelTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
            Text(
              text = "Collector Community",
              color = Color.White,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold
            )
          }

          FloatingOrb(
            size = 36.dp,
            primaryColor = AethelNeonCyan,
            secondaryColor = AethelPrimary
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Create Post / Share Observation Box
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
              value = newPostContent,
              onValueChange = { newPostContent = it },
              placeholder = { Text("Share curatorial notes, market insights, or exhibition reviews...", color = AethelTextMuted) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("community_post_input"),
              shape = RoundedCornerShape(14.dp),
              minLines = 2,
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AethelBackground,
                unfocusedContainerColor = AethelBackground,
                focusedBorderColor = AethelPrimary,
                unfocusedBorderColor = AethelBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Posting as Moyo Shiro (Curator)",
                color = AethelNeonCyan,
                fontSize = 11.sp
              )

              Button(
                onClick = {
                  if (newPostContent.isNotBlank()) {
                    val newPost = CommunityPost(
                      id = "post-${System.currentTimeMillis()}",
                      authorName = "Moyo Shiro",
                      authorLocation = "Geneva / Tokyo",
                      content = newPostContent,
                      artworkTitle = "Curator Observation",
                      timestamp = "Just now",
                      likes = 1,
                      commentsCount = 0,
                      isLiked = true
                    )
                    postList = listOf(newPost) + postList
                    newPostContent = ""
                  }
                },
                enabled = newPostContent.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AethelPrimary),
                modifier = Modifier.testTag("publish_post_btn")
              ) {
                Text("Publish to Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }

      // Community Posts Feed
      items(postList, key = { it.id }) { post ->
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurface),
          border = BorderStroke(1.dp, AethelBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("community_post_${post.id}")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            // Author row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .background(
                      Brush.linearGradient(listOf(AethelNeonPink, AethelNeonPurple)),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = post.authorName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = post.authorName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${post.authorLocation} • ${post.timestamp}",
                    color = AethelTextMuted,
                    fontSize = 11.sp
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = AethelSurfaceHighlight
              ) {
                Text(
                  text = post.artworkTitle,
                  color = AethelNeonCyan,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = post.content,
              color = AethelTextSecondary,
              fontSize = 13.sp,
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action row (Like, Comment, Share)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .clickable {
                      onToggleLike(post.id)
                      val updatedLiked = !post.isLiked
                      post.isLiked = updatedLiked
                      post.likes = if (updatedLiked) post.likes + 1 else post.likes - 1
                    }
                    .testTag("like_post_${post.id}")
                ) {
                  Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) AethelNeonPink else AethelTextMuted,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = post.likes.toString(),
                    color = if (post.isLiked) AethelNeonPink else AethelTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = AethelTextMuted,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = post.commentsCount.toString(),
                    color = AethelTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              IconButton(
                onClick = {
                  val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Curator insight by ${post.authorName} on Aethel Gallery: \"${post.content}\"")
                    type = "text/plain"
                  }
                  context.startActivity(Intent.createChooser(shareIntent, "Share Insight"))
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "Share",
                  tint = AethelTextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(100.dp))
      }
    }
  }
}
