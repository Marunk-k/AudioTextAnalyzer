package com.example.audiotext.repository;
import com.example.audiotext.model.*;import java.util.*;
public interface ProjectRepository { Project save(Project p); Optional<Project> findById(Long id); List<Project> findAll(); void update(Project p); }
