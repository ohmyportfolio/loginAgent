package net.mycorp.jimin.base.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.mycorp.jimin.base.domain.OcMap;

@JsonIgnoreProperties({ "encrypted_password", "session_id" })
public class OcUser extends OcMap implements UserDetails {
	
	private static final long serialVersionUID = 484645101971386837L;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getUsername() {
		return getString("id");
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	@JsonIgnore
	public String getPassword() {
		return getString("encrypted_password");
	}

	public String getId() {
		return getString("id");
	}

	@SuppressWarnings("unchecked")
	public List<String> getGroupIds() {
		return (List<String>)get("all_group_ids");
	}
	
	public boolean isAdmin() {
		return "admin".equals(get("user_type"));
	}
	

}
