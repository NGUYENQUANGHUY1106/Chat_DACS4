package server.model;

import java.util.HashSet;
import java.util.Set;

public class GroupInfo {
	public String groupName; // ID của nhóm (ví dụ: "laptrinh")
	public String groupFullName; // Tên hiển thị (ví dụ: "Nhóm: Lập trình")
	public Set<String> members = new HashSet<>();
}