package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Comment;

public interface comentarioRepo extends JpaRepository <Comment,String>{

}
