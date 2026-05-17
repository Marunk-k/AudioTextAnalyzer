package com.example.audiotext.service;

import com.example.audiotext.repository.UserDictionaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryService {
    private final UserDictionaryRepository repository;
    private final CurrentUserService currentUserService;

    public DictionaryService(UserDictionaryRepository repository, CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    public String currentUsername() { return currentUserService.username(); }
    public List<UserDictionaryRepository.Entry> listEntries(String login) { return repository.findByUserLogin(login); }
    public List<String> getFillerWordsForUser(String login) { return repository.findEnabledValuesByUserLogin(login); }
    public void addFillerWord(String login, String value) { repository.add(login, value); }
    public void deleteEntry(String login, Long id) { repository.delete(login, id); }
    public void toggleEntry(String login, Long id, boolean enabled) { repository.toggle(login, id, enabled); }
}
