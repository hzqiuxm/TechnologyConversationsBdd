describe('compositeClassesModule', function() {

    beforeEach(module('compositeClassesModule'));

    describe('compositeClassesCtrl controller', function() {

        var scope, modalInstance, form;
        var packageName = 'compositesClass.com.technologyconversations.bdd.steps';
        var className = 'WebStepsComposites';
        var compositeClasses = [
            {
                package: packageName,
                class: className
            }
        ];

        beforeEach(
            inject(function($rootScope, $compile, $injector, $controller) {
                scope = $rootScope.$new();
                $controller("compositeClassesCtrl", {
                    $scope: scope,
                    $http: $injector.get('$http'),
                    $modalInstance: modalInstance,
                    compositeClasses: compositeClasses});
                form = $compile('<form>')(scope);
            })
        );

        it('should put compositeClasses data to the scope', function() {
            expect(scope.compositeClasses).toBe(compositeClasses);
        });

        describe('compositeClassUrl function', function() {
            it('should return composites URL', function() {
                var url = scope.compositeClassUrl(packageName, className);
                expect(url).toEqual('/page/composites/' + packageName + "." + className);
            });
        });

        describe('classNamePattern function', function() {
            it('should return common function', function() {
                expect(scope.classNamePattern().toString()).toBe(classNamePattern().toString());
            });
        });

    });

});