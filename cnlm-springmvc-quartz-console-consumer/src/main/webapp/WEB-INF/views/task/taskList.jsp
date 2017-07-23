<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%--
  Created by IntelliJ IDEA.
  User: wei9.li@changhong.com
  Date: 2015/4/21
  Time: 13:33
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Task Schedule</title>
</head>
<body>
<div>
    <div class="row">
        <div class="col-xs-12 col-sm-6 col-md-10">
            <form class="navbar-form navbar-left" role="search" style="padding-left: 0">
                <div class="form-group">
                    <input type="text" class="form-control" id="search-content" placeholder="Search" name="name">
                </div>
                <button type="submit" class="btn btn-default glyphicon glyphicon-search" id="searchTask" ></button>
            </form>
        </div>
        <div class="col-xs-6 col-md-2">
            <button type="button" class="btn btn-primary pull-right" data-toggle="modal"
                    data-target=".bs-task-modal-sm"
                    data-whatever="New">New
            </button>
        </div>
    </div>


    <div style="padding-top: 10px">

        <table class="table table-bordered">
            <thead>
            <tr>
                <th>Id</th>
                <th>Name</th>
                <th>Group</th>
                <th>Cron Ex</th>
                <th>Description</th>
                <th>Sync</th>
                <th>Class</th>
                <th>Bean Id</th>
                <th>method</th>
                <th>operation</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="job" items="${taskPage.list}">
                <tr>
                    <td class="jobId">${job.jobId }</td>
                    <td class="jobName">${job.jobName }</td>
                    <td class="jobGroup">${job.jobGroup }</td>
                    <td class="cronExpression">${job.cronExpression }</td>
                    <td class="description">${job.description }</td>
                    <td class="isConcurrent">${job.isConcurrent }</td>
                    <td class="beanClass">${job.beanClass }</td>
                    <td class="springId">${job.springId }</td>
                    <td class="methodName">${job.methodName }</td>
                    <td>
                        <c:choose>
                            <c:when test="${job.jobStatus=='1' }">
                                <a class="glyphicon glyphicon-stop" href="javascript:;"
                                   onclick="changeTaskStatus('${job.jobId}','stop')"></a>
                            </c:when>
                            <c:otherwise>
                                <a class="glyphicon glyphicon-play" href="javascript:;"
                                   onclick="changeTaskStatus('${job.jobId}','start')"></a>
                            </c:otherwise>
                        </c:choose>
                        <a class="glyphicon glyphicon-refresh" href="javascript:;"
                           onclick="updateCron('${job.jobId}')"></a>
                        <a class="glyphicon glyphicon-trash" href="javascript:;" data-toggle="modal"
                           data-target=".confirm-modal-sm" data-modal-cmd="del" data-jobId=${job.jobId }></a>
                           </a>
                        <a class="glyphicon glyphicon-cog" data-toggle="modal" data-target=".bs-task-modal-sm"
                           data-whatever="Edit" data-jobid=${job.jobId}
                           ></a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <tags:pagination page="${taskPage}" paginationSize="5"/>
        <section id="demo">
            <%--<div id="page" class="m-pagination"></div>--%>
        </section>
    </div>
    <div class="modal fade bs-task-modal-sm" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel"
         aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="taskModalLabel">New message</h4>
                </div>
                <div class="modal-body">
                    <form>
                        <div class="form-group">
                            <label for="task-name" class="control-label">Name:</label>
                            <input type="text" class="form-control" id="task-name" placeholder="Enter Name">
                        </div>
                        <div class="form-group">
                            <label for="task-group" class="control-label">Group:</label>
                            <input class="form-control" id="task-group" placeholder="Enter group name"/>
                        </div>
                        <div class="form-group">
                            <label for="task-cron" class="control-label">Cron-Ex:</label>
                            <input type="text" class="form-control" id="task-cron" placeholder="Enter cron expression"/>
                        </div>

                        <div class="form-group">
                            <label class="control-label">Async:</label>
                            <label class="checkbox-inline">
                                <input type="radio" name="async" id="inlineCheckbox1" value="1" checked="checked"> Yes
                            </label>
                            <label class="checkbox-inline">
                                <input type="radio" name="async" id="inlineCheckbox2" value="0"> No
                            </label>
                        </div>
                        <div class="form-group">
                            <label for="task-class" class="control-label">Class:</label>
                            <input type="text" class="form-control" id="task-class" placeholder="Enter class name">
                        </div>
                        <div class="form-group">
                            <label for="task-bean" class="control-label">Bean Id:</label>
                            <input type="text" class="form-control" id="task-bean" placeholder="Enter spring bean id">
                        </div>
                        <div class="form-group">
                            <label for="task-method" class="control-label">Method:</label>
                            <input type="text" class="form-control" id="task-method" placeholder="Enter method name">
                        </div>
                        <div class="form-group">
                            <label for="task-desc" class="control-label">Description:</label>
                            <textarea type="text" class="form-control" id="task-desc"
                                      placeholder="Enter description"> </textarea>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    <button type="button" id="submitTask" class="btn btn-primary" >Submit</button>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- Small modal -->
<div class="modal fade confirm-modal-sm" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header" style="text-align: center">
                <%--<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span--%>
                <%--aria-hidden="true">&times;</span></button>--%>
                <h4 class="modal-title" id="deleteTaskModalLabel">Confirm delete Task</h4>
            </div>
            <div class="modal-footer" style="text-align: center">
                <button type="button" class="btn btn-default" data-dismiss="modal" style="width:48%">Cancel</button>
                <button type="button" id="deleteTask" class="btn btn-primary" data-jobId style="width:48%">Delete</button>
            </div>
        </div>
    </div>
</div>

<script src="${ctx}/bower_components/jquery/dist/jquery.min.js"></script>
<script src="${ctx}/bower_components/bootstrap/dist/js/bootstrap.min.js"></script>
<script>

    $('.confirm-modal-sm').on('show.bs.modal',function(event){
        var button = $(event.relatedTarget) // Button that triggered the modal
        $("#deleteTask").attr('data-jobId',button.attr("data-jobId"));
        var modal = $(this)
    });

    $('.bs-task-modal-sm').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget) // Button that triggered the modal
        var task = button.data('whatever') // Extract info from data-* attributes
        var modal = $(this)
        var jobId = button.data('jobid')
        modal.find('.modal-title').text(task + ' Task');
        /*编辑时填充表格对应数据到模态框*/
        if (task == 'Edit') {
            var el = $(event.relatedTarget).parent().parent();
            modal.find('#task-name').val(el.find('.jobName').text())
            modal.find('#task-group').val(el.find('.jobGroup').text())
            modal.find('#task-cron').val(el.find('.cronExpression').text())
            modal.find('#task-class').val(el.find('.beanClass').text());
            modal.find('#task-bean').val(el.find('.springId').text());
            modal.find('#task-method').val(el.find('.methodName').text());
            modal.find('#task-desc').val(el.find('.description').text());
            console.log(el.find('.methodName').val())
        } else/*新增时则清空模态框已填充的数据*/ {
            modal.find('input').val('');
            modal.find('textarea').val('');
            modal.find('#inlineCheckbox1').val('1')
            modal.find('#inlineCheckbox2').val('0')
        }
        $('#submitTask').on('click', function() {
            var async = document.getElementsByName("async");
            var isConcurrent;
            for(var i=0; i<async.length; i++) {
                if (async[i].checked) {
                    isConcurrent=async[i].value;
                }
            }
            var name = modal.find('#task-name').val();
            var group = modal.find('#task-group').val();
            var cron = modal.find('#task-cron').val();
            var beanClass = modal.find('#task-class').val();
            var springId = modal.find('#task-bean').val();
            var method = modal.find('#task-method').val();
            var description = modal.find('#task-desc').val();
            if (task == 'Edit') {
                var obj = {"jobId":jobId, "jobName":name, "jobGroup":group, "cronExpression":cron, "beanClass":beanClass, "isConcurrent":isConcurrent, "springId":springId, "methodName":method, "description":description}
                $.ajax({
                    url: '${ctx}/task/edit',
                    type: 'POST',
                    contentType: "application/json",
                    dataType: 'json',
                    data: JSON.stringify(obj),
                    success: function() {
                        $('.bs-task-modal-sm').modal('hide');
                        alert("任务修改成功");
                        window.location.reload();
                    },
                    error:function(xhr) {
                        $('.bs-task-modal-sm').modal('hide');
                        alert(xhr.status + " " + xhr.statusText);
                    }

                });
            } else {
                var obj = {"jobName":name, "jobGroup":group, "cronExpression":cron, "beanClass":beanClass, "isConcurrent":isConcurrent, "springId":springId, "methodName":method, "description":description}
                $.ajax({
                    url: '${ctx}/task',
                    type: 'POST',
                    contentType: "application/json",
                    dataType: 'json',
                    data: JSON.stringify(obj),
                    success: function() {
                        $('.bs-task-modal-sm').modal('hide');
                        alert("任务创建成功");
                        window.location.reload();
                    },
                    error:function(xhr) {
                        $('.bs-task-modal-sm').modal('hide');
                        alert(xhr.status + " " + xhr.statusText);
                    }

                });
            }

        })

    })



    var updateCron = function (jobId, cronExpression) {
        $.ajax({
            url: '${ctx}/task/' + jobId + '/cron',
            type: 'PUT',
            contentType: "application/json",
            dataType: 'json',
//            data: JSON.stringify([cronExpression]),
            success: function() {
                window.location.reload();
            }

        });

    }
    var changeTaskStatus = function (jobId, cmd) {
        console.log(cmd);
        $.ajax({
            url: '${ctx}/task/' + jobId + '/status',
            type: 'PUT',
            contentType: "application/json",
            dataType: 'json',
            data: JSON.stringify([cmd]),
            success: function () {
                window.location.reload();
            }
        });
    }

    /*delete job*/
    $('#deleteTask').click(function (event) {
//        alert("delete job id" + $(event.target).attr('data-jobId'));
        var jobId = $(event.target).attr('data-jobId');
        $.ajax({
            url: '${ctx}/task/' + jobId + '/deletion',
            type: 'DELETE',
            contentType: "application/json",
            dataType: 'json',
            success: function () {
                window.location.reload();
            }
        });
        $('.confirm-modal-sm').modal('hide');
    })


</script>
</body>
</html>
