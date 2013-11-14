/* global _:false, $j:false, Backbone:false, baseUrl:false */

window.SS = typeof window.SS === 'object' ? window.SS : {};

(function() {

  var PAGE_SIZE = 100;



  var Suggestions = Backbone.Collection.extend({

    initialize: function() {
      this.more = false;
      this.page = 0;
    },


    parse: function(r) {
      this.more = r.more;
      return r.results;
    },


    fetch: function(options) {
      this.data = _.extend({
            p: 1,
            ps: PAGE_SIZE
          }, options.data || {});

      var settings = _.extend({}, options, { data: this.data });
      Backbone.Collection.prototype.fetch.call(this, settings);
    },


    fetchNextPage: function(options) {
      if (this.more) {
        this.data.p += 1;
        var settings = _.extend({ remove: false }, options, { data: this.data });
        this.fetch(settings);
      }
    }

  });



  var UserSuggestions = Suggestions.extend({

    url: function() {
      return baseUrl + '/api/users/search?f=s2';
    }

  });



  var ProjectSuggestions = Suggestions.extend({

    url: function() {
      return baseUrl + '/api/resources/search?f=s2&q=TRK&display_key=true';
    }

  });



  var AjaxSelectDetailsFilterView = window.SS.DetailsSelectFilterView.extend({
    template: '#ajaxSelectFilterTemplate',


    events: function() {
      return _.extend({},
          window.SS.DetailsSelectFilterView.prototype.events.call(this),
          {
            'keyup .navigator-filter-search input': 'search'
          }
      );
    },


    initialize: function() {
      window.SS.DetailsSelectFilterView.prototype.initialize.apply(this, arguments);

    },


    onRender: function() {
      this.resetChoices();
    },


    search: function() {
      var that = this;
      this.query = this.$('.navigator-filter-search input').val();
      if (this.query.length > 1) {
        this.options.filterView.choices.fetch({
          data: {
            s: this.query,
            ps: PAGE_SIZE
          },
          success: function() {
            var choices = that.options.filterView.choices.reject(function(item) {
              return that.options.filterView.selection.findWhere({ id: item.get('id') });
            });
            that.options.filterView.choices.reset(choices);
            that.updateLists();
          }
        });
      } else {
        this.resetChoices();
        this.updateLists();
      }
    },


    resetChoices: function() {
      this.options.filterView.choices.reset([]);
    },


    onShow: function() {
      this.$('.navigator-filter-search input').focus();
    }

  });



  var AjaxSelectFilterView = window.SS.SelectFilterView.extend({

    isDefaultValue: function() {
      return this.selection.length === 0;
    }

  });



  var ProjectFilterView = AjaxSelectFilterView.extend({

    initialize: function() {
      window.SS.BaseFilterView.prototype.initialize.call(this, {
        detailsView: AjaxSelectDetailsFilterView
      });

      this.selection = new ProjectSuggestions();
      this.choices = new ProjectSuggestions();
    }

  });



  var AssigneeDetailsFilterView = AjaxSelectDetailsFilterView.extend({

    resetChoices: function() {
      this.options.filterView.choices.reset([{
        id: '<unassigned>',
        text: 'Unassigned'
      }]);
    },


    onShow: function() {
      this.$('.navigator-filter-search input').focus();
    }

  });



  var AssigneeFilterView = AjaxSelectFilterView.extend({

    initialize: function() {
      window.SS.BaseFilterView.prototype.initialize.call(this, {
        detailsView: AssigneeDetailsFilterView
      });

      this.selection = new UserSuggestions();
      this.choices = new UserSuggestions();
    }

  });



  var ReporterFilterView = AjaxSelectFilterView.extend({

    initialize: function() {
      window.SS.BaseFilterView.prototype.initialize.call(this, {
        detailsView: AjaxSelectDetailsFilterView
      });

      this.selection = new UserSuggestions();
      this.choices = new UserSuggestions();
    }

  });



  /*
   * Export public classes
   */

  _.extend(window.SS, {
    ProjectFilterView: ProjectFilterView,
    AssigneeFilterView: AssigneeFilterView,
    ReporterFilterView: ReporterFilterView
  });

})();
