package com.badmintonhub.premisessmartbe.controller;

import com.badmintonhub.premisessmartbe.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final JdbcTemplate jdbc;

    @GetMapping("/overview")
    public OverviewDTO overview() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM premises", Long.class);
        Double avg  = jdbc.queryForObject("SELECT AVG(price) FROM premises", Double.class);
        Long today  = jdbc.queryForObject(
                "SELECT COUNT(*) FROM premises WHERE DATE(created_at)=CURDATE()", Long.class);
        return new OverviewDTO(total==null?0:total, avg==null?0:avg, today==null?0:today);
    }

    @GetMapping("/avg-price-by-type")
    public List<PairDTO> avgPriceByType() {
        return jdbc.query("""
            SELECT COALESCE(business_type,'unknown') AS label, AVG(price) AS value
            FROM premises
            GROUP BY COALESCE(business_type,'unknown')
            ORDER BY value DESC
        """, (rs,i)-> new PairDTO(rs.getString("label"), rs.getDouble("value")));
    }

    @GetMapping("/avg-price-by-day")
    public List<DayValDTO> avgPriceByDay(@RequestParam(defaultValue="30") int days) {
        return jdbc.query("""
            SELECT DATE(created_at) AS d, AVG(price) AS v
            FROM premises
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
            GROUP BY DATE(created_at) ORDER BY d
        """, ps->ps.setInt(1,days),
                (rs,i)-> new DayValDTO(rs.getDate("d").toString(), rs.getDouble("v")));
    }

    @GetMapping("/count-by-day")
    public List<DayCountDTO> countByDay(@RequestParam(defaultValue="30") int days) {
        return jdbc.query("""
            SELECT DATE(created_at) AS d, COUNT(*) AS c
            FROM premises
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
            GROUP BY DATE(created_at) ORDER BY d
        """, ps->ps.setInt(1,days),
                (rs,i)-> new DayCountDTO(rs.getDate("d").toString(), rs.getLong("c")));
    }

    // ====== mới: Pie – tỷ trọng số bài đăng theo loại hình ======
    @GetMapping("/count-by-type")
    public List<CountDTO> countByType() {
        return jdbc.query("""
            SELECT COALESCE(business_type,'unknown') AS label, COUNT(*) AS cnt
            FROM premises
            GROUP BY COALESCE(business_type,'unknown')
            ORDER BY cnt DESC
        """, (rs,i)-> new CountDTO(rs.getString("label"), rs.getLong("cnt")));
    }

    @GetMapping("/top-users-by-type")
    public List<StackDTO> topUsersByType(@RequestParam(defaultValue = "5") int limit) {
        // NOTE: nếu bảng tài khoản của bạn tên khác "users" (vd: "user"), đổi lại trong LEFT JOIN
        String sql = """
      SELECT
         COALESCE(u.email, CONCAT('User#', p.user_id)) AS user,
         COALESCE(p.business_type, 'unknown')          AS type,
         COUNT(*)                                      AS cnt
      FROM premises p
      LEFT JOIN users u ON u.id = p.user_id
      JOIN (
         SELECT user_id
         FROM premises
         WHERE user_id IS NOT NULL
         GROUP BY user_id
         ORDER BY COUNT(*) DESC
         LIMIT ?
      ) tu ON tu.user_id = p.user_id
      GROUP BY user, type
      ORDER BY user, cnt DESC
      """;

        return jdbc.query(sql,
                ps -> ps.setInt(1, limit),
                (rs, i) -> new StackDTO(
                        rs.getString("user"),
                        rs.getString("type"),
                        rs.getLong("cnt")));
    }


    // ====== mới: Min/Max diện tích theo loại hình (để vẽ 2 series) ======
    @GetMapping("/area-range-by-type")
    public List<RangeDTO> areaRangeByType() {
        return jdbc.query("""
            SELECT COALESCE(business_type,'unknown') AS label,
                   MIN(`areaM2`) AS mn,
                   MAX(`areaM2`) AS mx
            FROM premises
            GROUP BY COALESCE(business_type,'unknown')
            ORDER BY mx DESC;
        """, (rs,i)-> new RangeDTO(rs.getString("label"),
                (Double)rs.getObject("mn"),
                (Double)rs.getObject("mx")));
    }
}
