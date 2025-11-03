package com.shootdoori.match.value;

import com.shootdoori.match.entity.lineup.LineupMember;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class LineupMembers {

    @OneToMany(
        mappedBy = "teamMember",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
        orphanRemoval = true
    )
    private List<LineupMember> lineupMembers = new ArrayList<>();

    protected LineupMembers() {
    }

    public LineupMembers(List<LineupMember> lineupMembers) {
        this.lineupMembers = lineupMembers == null
            ? new ArrayList<>()
            : new ArrayList<>(lineupMembers);
    }

    public static LineupMembers empty() { return new LineupMembers(); }

    public boolean isEmpty() { return lineupMembers.isEmpty(); }

    public void clear() {
        lineupMembers.clear();
    }

    public void addMember(LineupMember lineupMember) {
        lineupMembers.add(lineupMember);
    }

    public void removeMember(LineupMember lineupMember) {
        lineupMembers.remove(lineupMember);
    }
}
