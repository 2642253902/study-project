package com.exampe.mapper;

import com.exampe.entity.auth.Account;
import com.exampe.entity.user.AccountUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM db_account WHERE username = #{usernameOrEmail} OR email = #{usernameOrEmail}")
    Account findAccountByNameOrEmail(String usernameOrEmail);

    @Insert("INSERT INTO db_account(username,password,email) VALUES(#{username},#{password},#{email})")
    int createAccount(String username, String password, String email);

    @Update("UPDATE db_account SET password = #{password} WHERE email = #{email}")
    int restPasswordByEmail(String email, String password);


    @Select("SELECT * FROM db_account WHERE username = #{usernameOrEmail} OR email = #{usernameOrEmail}")
    AccountUser findAccountUserByNameOrEmail(String usernameOrEmail);

}
