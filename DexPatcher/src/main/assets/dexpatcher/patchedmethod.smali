.method %s %s%s
    .locals 6

    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    %s

    %s

    const-string v4, "%s"

    const-string v5, "%s"

    invoke-static {v4, v5, v3, v0}, Lcom/aliucord/patcher/Patcher;->runPrePatches(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/util/List;)Lcom/aliucord/patcher/PrePatchRes;

    move-result-object v1

    if-eqz v1, :cond_0

    iget-object v1, v1, Lcom/aliucord/patcher/PrePatchRes;->ret:Ljava/lang/Object;

    %s

    :cond_0
    %s

    invoke-%s {%s}, %s->%s%s

    %s

    invoke-static {v4, v5, v3, v0, v1}, Lcom/aliucord/patcher/Patcher;->runPatches(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/util/List;Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    %s
.end method
