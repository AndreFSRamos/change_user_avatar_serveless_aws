package pucpr.edu.avatar_generator.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pucpr.edu.avatar_generator.services.AvatarService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AvatarController {
    Logger logger = LogManager.getLogger(this.getClass().getName());
    private final AvatarService avatarService;

    @GetMapping("/{id}/avatar")
    public ResponseEntity<String> getAvatar(@PathVariable String id,
                                            @RequestParam String email,
                                            @RequestParam String name) {
        logger.info("[GET]::> /user/{}/avatar?email={}&name={}", id, email, name);
        return ResponseEntity.ok(avatarService.getOrCreateAvatar(id, email, name));
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<String> deleteAvatar(@PathVariable String id) {
        logger.info("[DELETE]::> /user/{}/avatar", id);
        return ResponseEntity.ok(avatarService.deleteAndRecreateAvatar(id));
    }
}