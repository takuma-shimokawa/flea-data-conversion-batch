package com.example.demo.repository;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.Category;

@Repository
public class CategoryRepository {

    private final NamedParameterJdbcTemplate template;

    // SpringからNamedParameterJdbcTemplateを受け取る
    public CategoryRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * categoryのparentIdとnameを条件に検索し、
     * 一致するcategory.idを取得する
     * @param category
     * @return
     */
    public List<Integer> findIdByParentIdAndName(Category category){
        // 検索条件として使用するカテゴリ名を取得
        String name = category.getName();
        // 検索条件として使用する親カテゴリIDを取得(最上位カテゴリの場合はNULL)
        Integer parentId = category.getParentId();

        // SQL内の名前付きパラメーターに渡す値を設定する
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("name", name);
        param.addValue("parentId", parentId);

        // 最上位カテゴリ用
        // 最上位には親が存在しないためparent_id IS NULLで検索する
        String sql1 = "SELECT id FROM category WHERE parent_id IS NULL AND name=:name;";
        // 子カテゴリ用
        // parent_idとnameの組み合わせで検索する
        String sql2 = "SELECT id FROM category WHERE parent_id=:parentId AND name=:name;";

        // query()の検索結果を受け取るList
        // 見つからなければ空のList、見つかればidが格納される
        List<Integer> id;

        if(parentId == null){
            // 最上位カテゴリを検索する
            // DBのid列をJavaのIntegerとして取り出し、List<Integer>で受け取る
            id = template.query(sql1,param,(rs,rowNum)-> rs.getInt("id"));
        }else{
            // 子カテゴリをparentIdとnameで検索する
            // DBのid列をJavaのIntegerとして取り出し、List<Integer>で受け取る
            id = template.query(sql2,param,(rs,rowNum)-> rs.getInt("id"));
        }
        // 検索結果を呼び出し元（CategoryWriter）へ返す
        return id;
    }

    /**
     * categoryテーブルにカテゴリを1件登録し、
     * DBで自動採番されたidを返す。
     * @param category
     * @return
     */
    public Integer insertAndGetId(Category category){

        // 登録するCategoryから各項目を取得する
        // 最上位カテゴリの場合、parentIdはnullになる
        Integer parentId = category.getParentId();
        String name = category.getName();
        String nameAll = category.getNameAll();

        // SQLの名前付きパラメータに渡す値を設定する
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("parentId", parentId);
        param.addValue("name", name);
        param.addValue("nameAll", nameAll);

        // idはSERIALによる自動採番なのでINSERT対象には含めない
        String sql = "INSERT INTO category (parent_id,name, name_all) VALUES(:parentId,:name,:nameAll);";

        // INSERT時にDBで自動採番されたidを受け取るための入れ物
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        // categoryをDBへ登録する
        // KeyHolderに自動採番された値を格納し、
        // new String[]{"id"}で取得したい自動採番カラムが「id」であることを指定する
        template.update(sql, param,keyHolder,new String[]{"id"});

        // KeyHolderから自動採番されたidを取り出す
        // getKey()はNumber型なので、intValue()でintに変換して返す
        // 戻り値のIntegerへは自動的に変換される
        return keyHolder.getKey().intValue();

    }
}
